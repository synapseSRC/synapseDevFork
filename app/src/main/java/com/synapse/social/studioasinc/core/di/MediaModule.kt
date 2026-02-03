package com.synapse.social.studioasinc.core.di

import android.content.Context
import com.synapse.social.studioasinc.core.media.MediaConfig
import com.synapse.social.studioasinc.core.media.MediaFacade
import com.synapse.social.studioasinc.core.media.cache.MediaCache
import com.synapse.social.studioasinc.core.media.processing.ImageCompressor
import com.synapse.social.studioasinc.core.media.processing.ImageProcessor
import com.synapse.social.studioasinc.core.media.processing.ThumbnailGenerator
import com.synapse.social.studioasinc.core.media.processing.VideoProcessor
import com.synapse.social.studioasinc.core.media.storage.MediaStorageService
import com.synapse.social.studioasinc.core.media.storage.MediaUploadCoordinator
import com.synapse.social.studioasinc.data.local.database.AppSettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideMediaConfig(): MediaConfig = MediaConfig()

    @Provides
    @Singleton
    fun provideMediaCache(@ApplicationContext context: Context): MediaCache = MediaCache(context)

    @Provides
    @Singleton
    fun provideImageCompressor(@ApplicationContext context: Context): ImageCompressor = ImageCompressor(context)

    @Provides
    @Singleton
    fun provideThumbnailGenerator(@ApplicationContext context: Context): ThumbnailGenerator = ThumbnailGenerator(context)

    @Provides
    @Singleton
    fun provideImageProcessor(
        @ApplicationContext context: Context,
        imageCompressor: ImageCompressor,
        config: MediaConfig
    ): ImageProcessor = ImageProcessor(context, imageCompressor, config)

    @Provides
    @Singleton
    fun provideVideoProcessor(
        @ApplicationContext context: Context,
        thumbnailGenerator: ThumbnailGenerator,
        config: MediaConfig
    ): VideoProcessor = VideoProcessor(context, thumbnailGenerator, config)

    @Provides
    @Singleton
    fun provideMediaStorageService(
        @ApplicationContext context: Context,
        appSettingsManager: AppSettingsManager
    ): MediaStorageService = MediaStorageService(context, appSettingsManager)

    @Provides
    @Singleton
    fun provideMediaFacade(
        storageService: MediaStorageService,
        imageProcessor: ImageProcessor,
        videoProcessor: VideoProcessor,
        config: MediaConfig
    ): MediaFacade = MediaFacade(storageService, imageProcessor, videoProcessor, config)

    @Provides
    @Singleton
    fun provideMediaUploadCoordinator(mediaFacade: MediaFacade): MediaUploadCoordinator =
        MediaUploadCoordinator(mediaFacade)
}
