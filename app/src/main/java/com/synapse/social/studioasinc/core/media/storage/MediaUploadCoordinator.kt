package com.synapse.social.studioasinc.core.media.storage

import android.net.Uri
import com.synapse.social.studioasinc.core.media.MediaFacade
import com.synapse.social.studioasinc.core.media.models.MediaUploadType
import com.synapse.social.studioasinc.domain.model.MediaItem
import com.synapse.social.studioasinc.domain.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinator for uploading multiple media items.
 * Renamed from MediaUploadManager.
 */
@Singleton
class MediaUploadCoordinator @Inject constructor(
    private val mediaFacade: MediaFacade
) {

    /**
     * Uploads multiple media items.
     */
    suspend fun uploadMultipleMedia(
        mediaItems: List<MediaItem>,
        onProgress: (Float) -> Unit,
        onComplete: (List<MediaItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val uploadedItems = mutableListOf<MediaItem>()

                mediaItems.forEachIndexed { index, mediaItem ->
                    val result = if (mediaItem.type == MediaType.IMAGE) {
                        mediaFacade.uploadImage(Uri.parse(mediaItem.url), MediaUploadType.POST)
                    } else if (mediaItem.type == MediaType.VIDEO) {
                        mediaFacade.uploadVideo(Uri.parse(mediaItem.url), MediaUploadType.POST)
                    } else {
                        Result.success(null)
                    }

                    if (result.isSuccess) {
                        val uploadResult = result.getOrNull()
                        if (uploadResult != null) {
                            uploadedItems.add(mediaItem.copy(
                                url = uploadResult.url,
                                mimeType = uploadResult.mimeType
                            ))
                        } else {
                            uploadedItems.add(mediaItem)
                        }
                    } else {
                        android.util.Log.w("MediaUpload", "Failed to upload ${mediaItem.url}: ${result.exceptionOrNull()?.message}")
                        uploadedItems.add(mediaItem)
                    }

                    val progress = (index + 1).toFloat() / mediaItems.size
                    withContext(Dispatchers.Main) {
                        onProgress(progress)
                    }
                }

                withContext(Dispatchers.Main) {
                    onComplete(uploadedItems)
                }
            } catch (e: Exception) {
                android.util.Log.e("MediaUpload", "Media upload failed", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Upload failed")
                }
            }
        }
    }
}
