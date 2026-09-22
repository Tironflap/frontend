package com.tironflap.frontend.data

import com.tironflap.frontend.data.model.SystemDef

object DefaultSystems {
    val list = listOf(
        SystemDef("nes", "Nintendo Entertainment System", "NES", "nes,fds,unf,unif", sortOrder = 10),
        SystemDef("snes", "Super Nintendo", "SNES", "sfc,smc,fig,swc,bs", sortOrder = 20),
        SystemDef("n64", "Nintendo 64", "N64", "n64,z64,v64", sortOrder = 30),
        SystemDef("gb", "Game Boy", "GB", "gb", sortOrder = 40),
        SystemDef("gbc", "Game Boy Color", "GBC", "gbc", sortOrder = 50),
        SystemDef("gba", "Game Boy Advance", "GBA", "gba", sortOrder = 60),
        SystemDef("nds", "Nintendo DS", "NDS", "nds,dsi", sortOrder = 70),
        SystemDef("3ds", "Nintendo 3DS", "3DS", "3ds,cia,cxi", sortOrder = 80),
        SystemDef("gc", "GameCube", "GC", "gcm,gcz", sortOrder = 90),
        SystemDef("wii", "Wii", "Wii", "wbfs,wia,wad", sortOrder = 100),
        SystemDef("wiiu", "Wii U", "Wii U", "wud,wux,wua,rpx", sortOrder = 110),
        SystemDef("switch", "Nintendo Switch", "Switch", "xci,nsp", sortOrder = 120),
        SystemDef("psx", "PlayStation", "PS1", "cue,pbp,chd", sortOrder = 130),
        SystemDef("ps2", "PlayStation 2", "PS2", "chd", sortOrder = 140),
        SystemDef("psp", "PlayStation Portable", "PSP", "cso", sortOrder = 150),
        SystemDef("psvita", "PlayStation Vita", "PS Vita", "vpk", sortOrder = 160),
        SystemDef("dreamcast", "Dreamcast", "DC", "cdi,gdi", sortOrder = 170),
        SystemDef("saturn", "Sega Saturn", "Saturn", "cue", sortOrder = 180),
        SystemDef("genesis", "Sega Genesis / Mega Drive", "Genesis", "md,gen,smd", sortOrder = 190),
        SystemDef("mastersystem", "Master System", "SMS", "sms", sortOrder = 200),
        SystemDef("gg", "Game Gear", "GG", "gg", sortOrder = 210),
        SystemDef("arcade", "Arcade", "Arcade", "zip,7z", sortOrder = 220),
        SystemDef("neogeo", "Neo Geo", "Neo Geo", "zip,7z", sortOrder = 230),
        SystemDef("pcengine", "PC Engine / TurboGrafx", "PCE", "pce,sgx", sortOrder = 240),
        SystemDef("wonderswan", "WonderSwan", "WS", "ws,wsc", sortOrder = 250),
        SystemDef("lynx", "Atari Lynx", "Lynx", "lnx", sortOrder = 260)
    )

    fun extensionToSystem(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        list.forEach { sys ->
            sys.extensionList().forEach { ext ->
                if (!map.containsKey(ext)) map[ext] = sys.id
            }
        }
        // Ambiguous – resolved in resolveAmbiguous / resolveIso
        map["iso"] = "unknown_iso"
        map["bin"] = "unknown_bin"
        map["img"] = "unknown_bin"
        return map
    }

    /**
     * Resolve ambiguous extensions.
     * .iso defaults to **PS2** (most common on Android handheld libraries).
     * GC only when size/name/folder clearly say GameCube.
     */
    fun resolveAmbiguous(
        ext: String,
        fileName: String,
        parentFolderName: String?,
        fileSize: Long = 0
    ): String? {
        val name = (fileName + " " + (parentFolderName ?: "")).lowercase()
        return when (ext) {
            "iso" -> resolveIso(name, fileSize)
            "bin", "img" -> "psx"
            "pbp" -> "psp"
            else -> null
        }
    }

    private fun resolveIso(name: String, fileSize: Long): String {
        // Explicit folder / tag wins
        when {
            name.contains("psp") || name.contains("(psn)") || name.contains(" ppsspp") -> return "psp"
            name.contains("ps2") || name.contains("playstation 2") || name.contains("psx2") -> return "ps2"
            name.contains("ps1") || name.contains("psx") || name.contains("playstation 1") -> return "psx"
            name.contains("gamecube") || name.contains("(gc)") || name.contains(" nintendo gamecube") -> return "gc"
            name.contains("wii") && !name.contains("wii u") -> return "wii"
            name.contains("dreamcast") || name.contains("(dc)") -> return "dreamcast"
        }

        // Well-known PS2 titles (common false-GC cases)
        val ps2Titles = listOf(
            "god of war", "gran turismo", "grand theft auto", "gta ", "san andreas",
            "max payne", "bully", "devil may cry", "deus ex", "metal gear solid",
            "silent hill", "resident evil", "tekken", "soulcalibur", "final fantasy",
            "kingdom hearts", "shadow of the colossus", "ico", "jak and daxter",
            "ratchet", "sly cooper", "crash bandicoot", "spyro", "mgs2", "mgs3",
            "batman begins", "lego batman", "black", "okami", "persona 3", "persona 4",
            "shin megami", "dragon quest", "xenosaga", "zone of the enders"
        )
        if (ps2Titles.any { name.contains(it) }) return "ps2"

        // Well-known PSP titles often stored as .iso
        val pspTitles = listOf(
            "locoroco", "chains of olympus", "ghost of sparta", "crisis core",
            "monster hunter freedom", "patapon", "lumines", "wipeout pure",
            "grand theft auto liberty city", "gta vice city stories", "tekken 6",
            "god of war: chains", "god of war: ghost"
        )
        if (pspTitles.any { name.contains(it) }) return "psp"

        // Well-known GameCube titles
        val gcTitles = listOf(
            "super smash bros melee", "melee", "wind waker", "twilight princess",
            "metroid prime", "resident evil 4", "super mario sunshine",
            "luigi's mansion", "paper mario", "pikmin", "f-zero gx",
            "animal crossing", "mario kart double dash", "soulcalibur ii"
        )
        if (gcTitles.any { name.contains(it) }) return "gc"

        // Size heuristic:
        // GameCube disc max ~1.4 GiB. Dual-layer PS2 games are often > 1.5 GiB.
        // Many single-layer PS2 games are still ~1–2 GB.
        if (fileSize > 0) {
            val mb = fileSize / (1024.0 * 1024.0)
            when {
                mb > 1500 -> return "ps2"          // almost never GC
                mb in 1200.0..1450.0 -> {
                    // borderline – prefer PS2 unless folder says GC
                    if (name.contains("gc") || name.contains("gamecube")) return "gc"
                    return "ps2"
                }
                mb < 700 -> return "psp"           // small ISOs often PSP
            }
        }

        // Default: PS2 (matches most Android ROM libraries)
        return "ps2"
    }
}
