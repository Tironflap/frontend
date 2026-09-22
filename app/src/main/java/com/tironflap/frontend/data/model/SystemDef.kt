package com.tironflap.frontend.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a game system / platform (NES, SNES, PS1, etc.)
 */
@Entity(tableName = "systems")
data class SystemDef(
    @PrimaryKey val id: String,          // "nes", "snes", "gba"...
    val name: String,                    // "Nintendo Entertainment System"
    val shortName: String,               // "NES"
    val extensions: String,              // comma-separated: "nes,fds,unf"
    val emulatorPackage: String? = null,
    val emulatorActivity: String? = null,
    val coreName: String? = null,        // RetroArch core if used
    val sortOrder: Int = 0,
    val isEnabled: Boolean = true
) {
    fun extensionList(): List<String> =
        extensions.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
}
