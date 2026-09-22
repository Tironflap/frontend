package com.tironflap.frontend.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.tironflap.frontend.data.DefaultSystems
import com.tironflap.frontend.data.db.GameDao
import com.tironflap.frontend.data.db.RomDirectoryDao
import com.tironflap.frontend.data.db.SystemDao
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
    private val scraper: GameScraper
) {
    val games: Flow<List<Game>> = gameDao.getAllGames()
    val directories: Flow<List<RomDirectory>> = romDirectoryDao.getAll()
    val systems: Flow<List<SystemDef>> = systemDao.getEnabledSystems()

    suspend fun ensureDefaultSystems() {
        val existing = systemDao.getById("nes")
        if (existing == null) {
            systemDao.insertAll(DefaultSystems.list)
        }
    }

    suspend fun addDirectory(uri: Uri, displayName: String, systemId: String? = null) {
        // Persist permission
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // May already have it or not persistable
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

    /**
     * Scans all configured ROM directories, detects games by extension,
     * inserts new ones, and runs the general scraper on them.
     */
    suspend fun scanAndScrape(onProgress: (String) -> Unit = {}): ScanResult =
        withContext(Dispatchers.IO) {
            ensureDefaultSystems()
            val extToSystem = DefaultSystems.extensionToSystem()
            val dirs = romDirectoryDao.getAllOnce()
            var added = 0
            var updated = 0
            var scraped = 0

            for (dir in dirs) {
                onProgress("Scanning ${dir.displayName}...")
                val root = DocumentFile.fromTreeUri(context, Uri.parse(dir.uri))
                    ?: continue

                val files = mutableListOf<DocumentFile>()
                collectRomFiles(root, dir.recursive, files)

                for (file in files) {
                    if (!file.isFile) continue
                    val name = file.name ?: continue
                    val ext = name.substringAfterLast('.', "").lowercase()
                    if (ext.isEmpty()) continue

                    val systemId = dir.systemId
                        ?: extToSystem[ext]
                        ?: continue   // unknown extension → skip

                    val path = file.uri.toString()
                    val existing = gameDao.getGameByPath(path)

                    if (existing == null) {
                        val cleanName = cleanRomName(name)
                        var game = Game(
                            name = cleanName,
                            path = path,
                            systemId = systemId,
                            fileName = name,
                            fileSize = file.length()
                        )

                        // Run general scraper
                        onProgress("Scraping: $cleanName")
                        val meta = scraper.scrape(cleanName, systemId)
                        if (meta != null) {
                            game = game.copy(
                                scrapedName = meta.name,
                                name = meta.name.ifBlank { cleanName },
                                description = meta.description,
                                releaseDate = meta.releaseDate,
                                developer = meta.developer,
                                publisher = meta.publisher,
                                genre = meta.genre,
                                coverUrl = meta.coverUrl,
                                screenshotUrl = meta.screenshotUrl,
                                rating = meta.rating
                            )
                            scraped++
                        }

                        gameDao.insert(game)
                        added++
                    } else {
                        // Optionally re-scrape missing metadata later
                        updated++
                    }
                }
            }

            onProgress("Done")
            ScanResult(added = added, existing = updated, scraped = scraped)
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

    /** Strip common dump tags and extension from filename */
    private fun cleanRomName(fileName: String): String {
        var name = fileName.substringBeforeLast('.')
        // Remove common tags: (USA), [!], (Rev 1), etc.
        name = name.replace(Regex("""\\s*[\\(\\[][^\\)\\]]*[\\)\\]]"""), "")
        name = name.replace(Regex("""[_\\.]+"""), " ")
        return name.trim()
    }

    data class ScanResult(
        val added: Int,
        val existing: Int,
        val scraped: Int
    )
}
