package com.tironflap.frontend.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tironflap.frontend.data.model.RomDirectory
import kotlinx.coroutines.flow.Flow

@Dao
interface RomDirectoryDao {
    @Query("SELECT * FROM rom_directories ORDER BY dateAdded ASC")
    fun getAll(): Flow<List<RomDirectory>>

    @Query("SELECT * FROM rom_directories")
    suspend fun getAllOnce(): List<RomDirectory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(directory: RomDirectory): Long

    @Delete
    suspend fun delete(directory: RomDirectory)

    @Query("DELETE FROM rom_directories WHERE id = :id")
    suspend fun deleteById(id: Long)
}
