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
        SystemDef("3ds", "Nintendo 3DS", "3DS", "3ds,cia,cxi,app", sortOrder = 80),
        SystemDef("gc", "GameCube", "GC", "iso,gcm,gcz,rvz", sortOrder = 90),
        SystemDef("wii", "Wii", "Wii", "iso,wbfs,rvz,wia,dol,elf", sortOrder = 100),
        SystemDef("wiiu", "Wii U", "Wii U", "wud,wux,wua,rpx,rpl", sortOrder = 110),
        SystemDef("switch", "Nintendo Switch", "Switch", "xci,nsp,nca", sortOrder = 120),
        SystemDef("psx", "PlayStation", "PS1", "bin,cue,img,iso,pbp,chd", sortOrder = 130),
        SystemDef("ps2", "PlayStation 2", "PS2", "iso,bin,chd,gz", sortOrder = 140),
        SystemDef("psp", "PlayStation Portable", "PSP", "iso,cso,pbp,prx", sortOrder = 150),
        SystemDef("psvita", "PlayStation Vita", "PS Vita", "vpk,mai", sortOrder = 160),
        SystemDef("dreamcast", "Dreamcast", "DC", "cdi,gdi,chd,iso", sortOrder = 170),
        SystemDef("saturn", "Sega Saturn", "Saturn", "cue,iso,chd", sortOrder = 180),
        SystemDef("genesis", "Sega Genesis / Mega Drive", "Genesis", "md,gen,smd,bin", sortOrder = 190),
        SystemDef("mastersystem", "Master System", "SMS", "sms", sortOrder = 200),
        SystemDef("gg", "Game Gear", "GG", "gg", sortOrder = 210),
        SystemDef("arcade", "Arcade", "Arcade", "zip,7z", sortOrder = 220),
        SystemDef("neogeo", "Neo Geo", "Neo Geo", "zip,7z", sortOrder = 230),
        SystemDef("pcengine", "PC Engine / TurboGrafx", "PCE", "pce,sgx,cue,chd", sortOrder = 240),
        SystemDef("wonderswan", "WonderSwan", "WS", "ws,wsc", sortOrder = 250),
        SystemDef("lynx", "Atari Lynx", "Lynx", "lnx", sortOrder = 260),
        SystemDef("jaguar", "Atari Jaguar", "Jaguar", "j64,jag", sortOrder = 270),
        SystemDef("3do", "3DO", "3DO", "iso,cue,chd", sortOrder = 280),
        SystemDef("amiga", "Amiga", "Amiga", "adf,ipf,dms,lha", sortOrder = 290),
        SystemDef("dos", "DOS", "DOS", "exe,com,bat", sortOrder = 300),
        SystemDef("scummvm", "ScummVM", "ScummVM", "", sortOrder = 310),
        SystemDef("android", "Android Apps", "Android", "", sortOrder = 900)
    )

    /** Map extension -> systemId (first match wins) */
    fun extensionToSystem(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        list.forEach { sys ->
            sys.extensionList().forEach { ext ->
                if (!map.containsKey(ext)) {
                    map[ext] = sys.id
                }
            }
        }
        return map
    }
}
