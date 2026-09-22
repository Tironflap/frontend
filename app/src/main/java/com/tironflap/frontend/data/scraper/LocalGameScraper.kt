package com.tironflap.frontend.data.scraper

import javax.inject.Inject
import javax.inject.Singleton

/**
 * General / local scraper.
 *
 * Currently does intelligent name cleaning and basic metadata inference.
 * Later this can be extended or replaced by ScreenScraper, IGDB, etc.
 * while keeping the same interface.
 */
@Singleton
class LocalGameScraper @Inject constructor() : GameScraper {

    override suspend fun scrape(name: String, systemId: String): ScrapedMetadata? {
        if (name.isBlank()) return null

        val cleaned = name
            .replace(Regex("""\\s+"""), " ")
            .trim()

        // Very lightweight “scrape”: title-case + system-aware hints
        val title = cleaned
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }

        return ScrapedMetadata(
            name = title,
            description = null,
            releaseDate = null,
            developer = null,
            publisher = null,
            genre = guessGenre(title, systemId),
            coverUrl = null,
            screenshotUrl = null,
            rating = null
        )
    }

    private fun guessGenre(title: String, systemId: String): String? {
        val t = title.lowercase()
        return when {
            t.contains("mario") || t.contains("sonic") || t.contains("crash") -> "Platformer"
            t.contains("zelda") || t.contains("metroid") -> "Action-Adventure"
            t.contains("final fantasy") || t.contains("chrono") || t.contains("dragon quest") -> "RPG"
            t.contains("street fighter") || t.contains("tekken") || t.contains("mortal kombat") -> "Fighting"
            t.contains("fifa") || t.contains("nba") || t.contains("madden") -> "Sports"
            t.contains("resident evil") || t.contains("silent hill") -> "Horror"
            systemId in listOf("arcade", "neogeo") -> "Arcade"
            else -> null
        }
    }
}
