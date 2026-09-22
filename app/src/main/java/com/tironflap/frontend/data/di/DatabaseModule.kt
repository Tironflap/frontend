package com.tironflap.frontend.data.di

import android.content.Context
import androidx.room.Room
import com.tironflap.frontend.data.db.AppDatabase
import com.tironflap.frontend.data.db.GameDao
import com.tironflap.frontend.data.db.RomDirectoryDao
import com.tironflap.frontend.data.db.SystemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "frontend.db"
        )
            .fallbackToDestructiveMigration() // early alpha – wipe on schema change
            .build()
    }

    @Provides
    fun provideGameDao(db: AppDatabase): GameDao = db.gameDao()

    @Provides
    fun provideSystemDao(db: AppDatabase): SystemDao = db.systemDao()

    @Provides
    fun provideRomDirectoryDao(db: AppDatabase): RomDirectoryDao = db.romDirectoryDao()
}
