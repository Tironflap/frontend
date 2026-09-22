package com.tironflap.frontend.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rom_directories")
data class RomDirectory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,                     // content:// URI from SAF
    val displayName: String,
    val systemId: String? = null,        // null = auto-detect by extension
    val recursive: Boolean = true,
    val dateAdded: Long = System.currentTimeMillis()
)
