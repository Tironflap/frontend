package com.tironflap.frontend.data.scraper

/**
 * General scraper interface.
 * Implementations can be local (name parsing), ScreenScraper, libretro, etc.
 */
interface GameScraper {
    /**
     * Try to find metadata for a game.
     * @param name cleaned ROM name
     * @param systemId internal system id (nes, snes, ...)
     * @return metadata or null if nothing found
     */
    suspend fun scrape(name: String, systemId: String): ScrapedMetadata?
}

data class ScrapedMetadata(
    val name: String,
    val description: String? = null,
    val releaseDate: String? = null,
    val developer: String? = null,
    val publisher: String? = null,
    val genre: String? = null,
    val coverUrl: String? = null,
    val screenshotUrl: String? = null,
    val rating: Float? = null
)
