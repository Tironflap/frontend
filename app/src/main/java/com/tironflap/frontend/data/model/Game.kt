package com.tironflap.frontend.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val path: String,                    // content URI or file path
    val systemId: String,                // e.g. "nes", "snes", "ps1"
    val fileName: String,
    val fileSize: Long = 0,
    val scrapedName: String? = null,
    val description: String? = null,
    val releaseDate: String? = null,
    val developer: String? = null,
    val publisher: String? = null,
    val genre: String? = null,
    val coverUrl: String? = null,
    val screenshotUrl: String? = null,
    val rating: Float? = null,
    val isFavorite: Boolean = false,
    val lastPlayed: Long? = null,
    val playCount: Int = 0,
    val dateAdded: Long = System.currentTimeMillis()
)
