package com.tironflap.frontend.data.hash

/**
 * Filters out non-game files that commonly pollute ROM folders
 * (EBOOT.PBP, DATA.BIN, track dumps, serial-only names, etc.)
 */
object JunkFilter {

    private val junkExactNames = setOf(
        "eboot.pbp",
        "param.sfo",
        "data.bin",
        "acdata.bin",
        "update.bin",
        "disc.sys",
        "system.cnf",
        "psdl.dat",
        "toc",
        "track01.bin",
        "track02.bin",
        "track03.bin",
        "track04.bin",
        "track05.bin"
    )

    private val junkExtensions = setOf(
        "sfo", "sys", "toc", "ccd", "sub", "img.mdf", "mds"
    )

    // Pure hex / serial looking names (3DS title IDs, random hashes, etc.)
    private val serialOrHashRegex = Regex(
        """^(?:0{3,}|[0-9A-Fa-f]{8,16}|[0-9A-Fa-f]{16})$"""
    )

    // PSP/PS3 style EBOOT without a meaningful parent folder name
    private val ebootRegex = Regex("""^eboot\\.pbp$""", RegexOption.IGNORE_CASE)

    fun isJunk(fileName: String, fileSize: Long = 0): Boolean {
        val lower = fileName.lowercase()
        val base = lower.substringBeforeLast('.')
        val ext = lower.substringAfterLast('.', "")

        if (lower in junkExactNames) return true
        if (ext in junkExtensions) return true
        if (ebootRegex.matches(lower)) return true

        // Names that are only hex / title IDs
        if (serialOrHashRegex.matches(base.replace(" ", ""))) return true

        // Extremely small "bin" files are usually not full games
        if (ext == "bin" && fileSize in 1 until 32_768) return true

        // Generic track names
        if (base.matches(Regex("""track\\d{2}"""))) return true

        return false
    }

    /**
     * Prefer certain file types when multiple files represent the same game.
     * Higher = better.
     */
    fun preferenceScore(fileName: String, systemId: String): Int {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (systemId) {
            "psx" -> when (ext) {
                "chd", "pbp" -> 100
                "cue" -> 90
                "iso" -> 70
                "bin", "img" -> 20   // prefer cue over raw bin
                else -> 50
            }
            "psp" -> when (ext) {
                "iso", "cso" -> 100
                "pbp" -> 40          // often EBOOT, lower priority
                else -> 50
            }
            "gc", "wii" -> when (ext) {
                "rvz", "gcz" -> 100
                "iso", "wbfs" -> 80
                else -> 50
            }
            else -> 50
        }
    }
}
