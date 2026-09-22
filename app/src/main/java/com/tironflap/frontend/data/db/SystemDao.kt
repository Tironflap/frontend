package com.tironflap.frontend.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tironflap.frontend.data.model.SystemDef
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemDao {
    @Query("SELECT * FROM systems WHERE isEnabled = 1 ORDER BY sortOrder ASC, name ASC")
    fun getEnabledSystems(): Flow<List<SystemDef>>

    @Query("SELECT * FROM systems ORDER BY sortOrder ASC, name ASC")
    fun getAllSystems(): Flow<List<SystemDef>>

    @Query("SELECT * FROM systems WHERE id = :id")
    suspend fun getById(id: String): SystemDef?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(systems: List<SystemDef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(system: SystemDef)
}
