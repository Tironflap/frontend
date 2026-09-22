package com.tironflap.frontend.data.scraper

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scrapes game titles + cover / title-screen art via the public
 * libretro-thumbnails CDN (no API key required).
 *
 * https://thumbnails.libretro.com/{System}/Named_Boxarts/{Game}.png
 */
@Singleton
class LibretroThumbnailScraper @Inject constructor() : GameScraper {

    override suspend fun scrape(name: String, systemId: String): ScrapedMetadata? {
        if (name.isBlank()) return null

        val title = prettyTitle(name)
        val systemFolder = systemFolder(systemId) ?: return ScrapedMetadata(
            name = title,
            genre = guessGenre(title, systemId)
        )

        val candidates = nameCandidates(title)
        val coverUrl = candidates.firstNotNullOfOrNull { candidate ->
            boxartUrl(systemFolder, candidate)
        }
        val shotUrl = candidates.firstNotNullOfOrNull { candidate ->
            titleScreenUrl(systemFolder, candidate)
        }

        return ScrapedMetadata(
            name = title,
            genre = guessGenre(title, systemId),
            coverUrl = coverUrl,
            screenshotUrl = shotUrl
        )
    }

    private fun prettyTitle(name: String): String {
        return name
            .replace(Regex("\\s+"), " ")
            .trim()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }

    /** Try several filename variants – libretro names are picky. */
    private fun nameCandidates(title: String): List<String> {
        val base = title.trim()
        return listOf(
            base,
            base.replace(" - ", " "),
            base.replace(":", " -"),
            base.replace(":", ""),
            base.replace("'", ""),
            base.replace(".", "")
        ).distinct()
    }

    private fun boxartUrl(systemFolder: String, gameName: String): String {
        val encSystem = encodePath(systemFolder)
        val encGame = encodePath(gameName)
        return "$BASE/$encSystem/Named_Boxarts/$encGame.png"
    }

    private fun titleScreenUrl(systemFolder: String, gameName: String): String {
        val encSystem = encodePath(systemFolder)
        val encGame = encodePath(gameName)
        return "$BASE/$encSystem/Named_Titles/$encGame.png"
    }

    /** Encode each path segment but keep spaces as %20 (not +). */
    private fun encodePath(value: String): String {
        return value.split("/").joinToString("/") { part ->
            URLEncoder.encode(part, StandardCharsets.UTF_8.name())
                .replace("+", "%20")
        }
    }

    private fun systemFolder(systemId: String): String? = when (systemId) {
        "nes" -> "Nintendo - Nintendo Entertainment System"
        "snes" -> "Nintendo - Super Nintendo Entertainment System"
        "n64" -> "Nintendo - Nintendo 64"
        "gb" -> "Nintendo - Game Boy"
        "gbc" -> "Nintendo - Game Boy Color"
        "gba" -> "Nintendo - Game Boy Advance"
        "nds" -> "Nintendo - Nintendo DS"
        "3ds" -> "Nintendo - Nintendo 3DS"
        "gc" -> "Nintendo - GameCube"
        "wii" -> "Nintendo - Wii"
        "wiiu" -> "Nintendo - Wii U"
        "switch" -> "Nintendo - Switch"
        "psx" -> "Sony - PlayStation"
        "ps2" -> "Sony - PlayStation 2"
        "psp" -> "Sony - PlayStation Portable"
        "psvita" -> "Sony - PlayStation Vita"
        "dreamcast" -> "Sega - Dreamcast"
        "saturn" -> "Sega - Saturn"
        "genesis" -> "Sega - Mega Drive - Genesis"
        "mastersystem" -> "Sega - Master System - Mark III"
        "gg" -> "Sega - Game Gear"
        "arcade" -> "MAME"
        "neogeo" -> "SNK - Neo Geo"
        "pcengine" -> "NEC - PC Engine - TurboGrafx 16"
        "wonderswan" -> "Bandai - WonderSwan"
        "lynx" -> "Atari - Lynx"
        else -> null
    }

    private fun guessGenre(title: String, systemId: String): String? {
        val t = title.lowercase()
        return when {
            t.contains("mario") || t.contains("sonic") || t.contains("crash") || t.contains("locoroco") -> "Platformer"
            t.contains("zelda") || t.contains("metroid") -> "Action-Adventure"
            t.contains("final fantasy") || t.contains("chrono") || t.contains("dragon quest") || t.contains("persona") -> "RPG"
            t.contains("street fighter") || t.contains("tekken") || t.contains("mortal kombat") -> "Fighting"
            t.contains("fifa") || t.contains("nba") || t.contains("madden") || t.contains("gran turismo") -> "Sports"
            t.contains("resident evil") || t.contains("silent hill") -> "Horror"
            t.contains("god of war") -> "Action"
            systemId in listOf("arcade", "neogeo") -> "Arcade"
            else -> null
        }
    }

    companion object {
        private const val BASE = "https://thumbnails.libretro.com"
    }
}
