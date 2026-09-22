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
        SystemDef("gc", "GameCube", "GC", "gcm,gcz", sortOrder = 90),          // iso handled specially
        SystemDef("wii", "Wii", "Wii", "wbfs,wia,wad", sortOrder = 100),
        SystemDef("wiiu", "Wii U", "Wii U", "wud,wux,wua,rpx", sortOrder = 110),
        SystemDef("switch", "Nintendo Switch", "Switch", "xci,nsp", sortOrder = 120),
        SystemDef("psx", "PlayStation", "PS1", "cue,pbp,chd", sortOrder = 130), // bin only with cue
        SystemDef("ps2", "PlayStation 2", "PS2", "chd", sortOrder = 140),
        SystemDef("psp", "PlayStation Portable", "PSP", "cso", sortOrder = 150),
        SystemDef("psvita", "PlayStation Vita", "PS Vita", "vpk", sortOrder = 160),
        SystemDef("dreamcast", "Dreamcast", "DC", "cdi,gdi,chd", sortOrder = 170),
        SystemDef("saturn", "Sega Saturn", "Saturn", "cue,chd", sortOrder = 180),
        SystemDef("genesis", "Sega Genesis / Mega Drive", "Genesis", "md,gen,smd", sortOrder = 190),
        SystemDef("mastersystem", "Master System", "SMS", "sms", sortOrder = 200),
        SystemDef("gg", "Game Gear", "GG", "gg", sortOrder = 210),
        SystemDef("arcade", "Arcade", "Arcade", "zip,7z", sortOrder = 220),
        SystemDef("neogeo", "Neo Geo", "Neo Geo", "zip,7z", sortOrder = 230),
        SystemDef("pcengine", "PC Engine / TurboGrafx", "PCE", "pce,sgx", sortOrder = 240),
        SystemDef("wonderswan", "WonderSwan", "WS", "ws,wsc", sortOrder = 250),
        SystemDef("lynx", "Atari Lynx", "Lynx", "lnx", sortOrder = 260)
    )

    /**
     * Extension → system. More specific extensions first.
     * Ambiguous ones (iso, bin) are resolved with extra logic in the scanner.
     */
    fun extensionToSystem(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        list.forEach { sys ->
            sys.extensionList().forEach { ext ->
                if (!map.containsKey(ext)) map[ext] = sys.id
            }
        }
        // Ambiguous but common
        map.putIfAbsent("iso", "unknown_iso")
        map.putIfAbsent("bin", "unknown_bin")
        map.putIfAbsent("img", "unknown_bin")
        return map
    }

    fun resolveAmbiguous(
        ext: String,
        fileName: String,
        parentFolderName: String?
    ): String? {
        val name = (fileName + " " + (parentFolderName ?: "")).lowercase()
        return when (ext) {
            "iso" -> when {
                name.contains("psp") || name.contains("(psn)") -> "psp"
                name.contains("ps2") || name.contains("playstation 2") -> "ps2"
                name.contains("ps1") || name.contains("psx") || name.contains("playstation") -> "psx"
                name.contains("gamecube") || name.contains("(gc)") || name.contains(" god of war") -> "gc"
                name.contains("wii") -> "wii"
                name.contains("dreamcast") || name.contains("(dc)") -> "dreamcast"
                // GameCube games often have (USA)/(Europe) and .iso – default GC for large isos is weak;
                // prefer leaving as null so user can assign, but for now guess GC for mid-size
                else -> "gc"
            }
            "bin", "img" -> when {
                // Prefer PSX only if not a tiny track file
                name.contains(".cue") -> "psx"
                else -> "psx" // most bin dumps in retro folders are PS1
            }
            "pbp" -> "psp"
            else -> null
        }
    }
}
