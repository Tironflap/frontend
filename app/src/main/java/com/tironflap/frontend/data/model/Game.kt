package com.tironflap.frontend.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val path: String,
    val systemId: String,
    val fileName: String,
    val fileSize: Long = 0,
    val crc32: String? = null,           // uppercase hex CRC32
    val md5: String? = null,
    val scrapedName: String? = null,
    val description: String? = null,
    val releaseDate: String? = null,
    val developer: String? = null,
    val publisher: String? = null,
    val genre: String? = null,
    val coverUrl: String? = null,
    val screenshotUrl: String? = null,
    val rating: Float? = null,
    val isVerified: Boolean = false,     // matched against known hash DB
    val isFavorite: Boolean = false,
    val lastPlayed: Long? = null,
    val playCount: Int = 0,
    val dateAdded: Long = System.currentTimeMillis()
)
