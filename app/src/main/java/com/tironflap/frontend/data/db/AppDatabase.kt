package com.tironflap.frontend.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tironflap.frontend.data.model.Game
import com.tironflap.frontend.data.model.RomDirectory
import com.tironflap.frontend.data.model.SystemDef

@Database(
    entities = [Game::class, SystemDef::class, RomDirectory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun systemDao(): SystemDao
    abstract fun romDirectoryDao(): RomDirectoryDao
}
