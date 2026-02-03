package com.synapse.social.studioasinc.core.util

import android.content.Context
import com.synapse.social.studioasinc.core.media.MediaConfig
import com.synapse.social.studioasinc.core.media.MediaFacade
import com.synapse.social.studioasinc.core.media.processing.ImageCompressor
import com.synapse.social.studioasinc.core.media.processing.ImageProcessor
import com.synapse.social.studioasinc.core.media.processing.ThumbnailGenerator
import com.synapse.social.studioasinc.core.media.processing.VideoProcessor
import com.synapse.social.studioasinc.core.media.storage.MediaStorageService
import com.synapse.social.studioasinc.core.media.storage.MediaUploadCoordinator
import com.synapse.social.studioasinc.data.local.database.AppSettingsManager
import com.synapse.social.studioasinc.domain.model.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @deprecated Use MediaUploadCoordinator instead.
 */
@Deprecated(
    message = "Use MediaUploadCoordinator instead",
    replaceWith = ReplaceWith(
        "mediaUploadCoordinator.uploadMultipleMedia(mediaItems, onProgress, onComplete, onError)",
        "com.synapse.social.studioasinc.core.media.storage.MediaUploadCoordinator"
    )
)
object MediaUploadManager {

    /**
     * Uploads multiple media items.
     * @deprecated Use MediaUploadCoordinator instead
     */
    fun uploadMultipleMedia(
        context: Context,
        mediaItems: List<MediaItem>,
        onProgress: (Float) -> Unit,
        onComplete: (List<MediaItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        val appSettingsManager = AppSettingsManager.getInstance(context)
        val imageCompressor = ImageCompressor(context)
        val storageService = MediaStorageService(context, appSettingsManager, imageCompressor)
        val config = MediaConfig()
        val imageProcessor = ImageProcessor(context, ImageCompressor(context), config)
        val videoProcessor = VideoProcessor(context, ThumbnailGenerator(context), config)
        val facade = MediaFacade(storageService, imageProcessor, videoProcessor, config)
        val coordinator = MediaUploadCoordinator(facade)

        CoroutineScope(Dispatchers.IO).launch {
            coordinator.uploadMultipleMedia(mediaItems, onProgress, onComplete, onError)
        }
    }
}
