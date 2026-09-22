package com.tironflap.frontend.data.di

import com.tironflap.frontend.data.scraper.GameScraper
import com.tironflap.frontend.data.scraper.LibretroThumbnailScraper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScraperModule {

    @Binds
    @Singleton
    abstract fun bindGameScraper(impl: LibretroThumbnailScraper): GameScraper
}
