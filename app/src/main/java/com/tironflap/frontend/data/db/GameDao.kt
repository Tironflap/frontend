package com.tironflap.frontend.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tironflap.frontend.data.model.Game
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY name ASC")
    fun getAllGames(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE systemId = :systemId ORDER BY name ASC")
    fun getGamesBySystem(systemId: String): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavorites(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: Long): Game?

    @Query("SELECT * FROM games WHERE path = :path LIMIT 1")
    suspend fun getGameByPath(path: String): Game?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: Game): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<Game>)

    @Update
    suspend fun update(game: Game)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM games")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM games")
    suspend fun count(): Int
}
