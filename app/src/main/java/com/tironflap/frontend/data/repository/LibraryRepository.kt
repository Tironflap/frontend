package com.tironflap.frontend.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.tironflap.frontend.data.DefaultSystems
import com.tironflap.frontend.data.db.GameDao
import com.tironflap.frontend.data.db.RomDirectoryDao
import com.tironflap.frontend.data.db.SystemDao
import com.tironflap.frontend.data.hash.HashDatabase
import com.tironflap.frontend.data.hash.JunkFilter
import com.tironflap.frontend.data.hash.RomHasher
import com.tironflap.frontend.data.model.Game
import com.tironflap.frontend.data.model.RomDirectory
import com.tironflap.frontend.data.model.SystemDef
import com.tironflap.frontend.data.scraper.GameScraper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameDao: GameDao,
    private val systemDao: SystemDao,
    private val romDirectoryDao: RomDirectoryDao,
    private val scraper: GameScraper,
    private val hasher: RomHasher,
    private val hashDb: HashDatabase
) {
    val games: Flow<List<Game>> = gameDao.getAllGames()
    val directories: Flow<List<RomDirectory>> = romDirectoryDao.getAll()
    val systems: Flow<List<SystemDef>> = systemDao.getEnabledSystems()

    private data class Candidate(
        val file: DocumentFile,
        val systemId: String,
        val parentName: String?
    )

    suspend fun ensureDefaultSystems() {
        val existing = systemDao.getById("nes")
        if (existing == null) {
            systemDao.insertAll(DefaultSystems.list)
        }
    }

    suspend fun addDirectory(uri: Uri, displayName: String, systemId: String? = null) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        }

        romDirectoryDao.insert(
            RomDirectory(
                uri = uri.toString(),
                displayName = displayName,
                systemId = systemId,
                recursive = true
            )
        )
    }

    suspend fun removeDirectory(directory: RomDirectory) {
        romDirectoryDao.delete(directory)
    }

    suspend fun clearLibrary() {
        gameDao.deleteAll()
    }

    /**
     * Full scan:
     * 1. Collect files
     * 2. Drop junk (EBOOT, DATA.BIN, serial-only names, tiny bins…)
     * 3. Resolve system (extension + folder heuristics)
     * 4. Prefer cue over bin, better formats first
     * 5. Hash with CRC32 and look up public-style hash DB
     * 6. Scrape remaining metadata
     */
    suspend fun scanAndScrape(onProgress: (String) -> Unit = {}): ScanResult =
        withContext(Dispatchers.IO) {
            ensureDefaultSystems()
            val extToSystem = DefaultSystems.extensionToSystem()
            val dirs = romDirectoryDao.getAllOnce()
            var added = 0
            var skippedJunk = 0
            var scraped = 0
            var verified = 0

            val candidates = mutableListOf<Candidate>()

            for (dir in dirs) {
                onProgress("Scanning ${dir.displayName}...")
                val root = DocumentFile.fromTreeUri(context, Uri.parse(dir.uri)) ?: continue
                val files = mutableListOf<DocumentFile>()
                collectRomFiles(root, dir.recursive, files)

                for (file in files) {
                    if (!file.isFile) continue
                    val name = file.name ?: continue
                    val size = file.length()

                    if (JunkFilter.isJunk(name, size)) {
                        skippedJunk++
                        continue
                    }

                    val ext = name.substringAfterLast('.', "").lowercase()
                    if (ext.isEmpty()) continue

                    val parentName = file.parentFile?.name
                    var systemId = dir.systemId ?: extToSystem[ext]

                    if (systemId == null || systemId.startsWith("unknown_")) {
                        systemId = DefaultSystems.resolveAmbiguous(ext, name, parentName)
                    }
                    if (systemId == null || systemId.startsWith("unknown_")) continue

                    candidates.add(Candidate(file, systemId, parentName))
                }
            }

            val filtered = preferBestFiles(candidates)

            for (c in filtered) {
                val file = c.file
                val name = file.name ?: continue
                val path = file.uri.toString()

                if (gameDao.getGameByPath(path) != null) continue

                onProgress("Hashing: $name")
                val crc = hasher.crc32(file.uri)

                val hashHit = hashDb.lookup(crc)
                val isVerified = hashHit != null

                val systemId = hashHit?.systemId ?: c.systemId
                val displayName = hashHit?.name ?: cleanRomName(name)

                if (displayName.isBlank() || displayName.length < 2) {
                    skippedJunk++
                    continue
                }
                if (JunkFilter.isJunk("$displayName.bin")) {
                    skippedJunk++
                    continue
                }

                onProgress("Scraping: $displayName")
                val meta = scraper.scrape(displayName, systemId)

                val game = Game(
                    name = meta?.name?.ifBlank { displayName } ?: displayName,
                    path = path,
                    systemId = systemId,
                    fileName = name,
                    fileSize = file.length(),
                    crc32 = crc,
                    scrapedName = meta?.name,
                    description = meta?.description,
                    releaseDate = meta?.releaseDate,
                    developer = meta?.developer,
                    publisher = meta?.publisher,
                    genre = meta?.genre,
                    coverUrl = meta?.coverUrl,
                    screenshotUrl = meta?.screenshotUrl,
                    rating = meta?.rating,
                    isVerified = isVerified
                )

                if (isVerified) verified++
                if (meta != null) scraped++

                gameDao.insert(game)
                added++
            }

            onProgress("Done – $added added, $skippedJunk junk skipped, $verified verified by hash")
            ScanResult(
                added = added,
                existing = 0,
                scraped = scraped,
                skippedJunk = skippedJunk,
                verified = verified
            )
        }

    private fun preferBestFiles(candidates: List<Candidate>): List<Candidate> {
        val groups = candidates.groupBy { c ->
            val base = cleanRomName(c.file.name ?: "")
            "${c.systemId}|$base"
        }

        return groups.values.mapNotNull { group ->
            group.maxByOrNull { c ->
                JunkFilter.preferenceScore(c.file.name ?: "", c.systemId)
            }
        }
    }

    private fun collectRomFiles(
        dir: DocumentFile,
        recursive: Boolean,
        out: MutableList<DocumentFile>
    ) {
        val children = dir.listFiles() ?: return
        for (child in children) {
            when {
                child.isFile -> out.add(child)
                child.isDirectory && recursive -> collectRomFiles(child, true, out)
            }
        }
    }

    private fun cleanRomName(fileName: String): String {
        var name = fileName.substringBeforeLast('.')
        name = name.replace(Regex("""\\s*[\\(\\[][^\\)\\]]*[\\)\\]]"""), "")
        name = name.replace(Regex("""[_\\.]+"""), " ")
        return name.trim()
    }

    data class ScanResult(
        val added: Int,
        val existing: Int,
        val scraped: Int,
        val skippedJunk: Int = 0,
        val verified: Int = 0
    )
}
