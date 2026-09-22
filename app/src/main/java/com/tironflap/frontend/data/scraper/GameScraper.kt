package com.tironflap.frontend.data.scraper

/**
 * General scraper interface.
 * Implementations can be local, libretro-thumbnails, ScreenScraper, etc.
 */
interface GameScraper {
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
