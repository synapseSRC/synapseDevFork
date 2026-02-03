package com.synapse.social.studioasinc.core.media

import android.content.Context
import android.net.Uri
import com.synapse.social.studioasinc.core.media.models.MediaUploadResult
import com.synapse.social.studioasinc.core.media.models.MediaUploadType
import com.synapse.social.studioasinc.core.media.processing.ImageProcessor
import com.synapse.social.studioasinc.core.media.processing.VideoProcessor
import com.synapse.social.studioasinc.core.media.storage.MediaStorageCallback
import com.synapse.social.studioasinc.core.media.storage.MediaStorageService
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Primary entry point for ALL media operations in Synapse Social.
 */
@Singleton
class MediaFacade @Inject constructor(
    private val storageService: MediaStorageService,
    private val imageProcessor: ImageProcessor,
    private val videoProcessor: VideoProcessor,
    private val config: MediaConfig
) {
    /**
     * Upload an image with automatic compression and optimization.
     */
    suspend fun uploadImage(
        uri: Uri,
        type: MediaUploadType,
        onProgress: ((Float) -> Unit)? = null
    ): Result<MediaUploadResult> {
        val processedResult = imageProcessor.processImage(uri)
        if (processedResult.isFailure) return Result.failure(processedResult.exceptionOrNull()!!)

        val processed = processedResult.getOrNull()!!

        return uploadFile(processed.file, processed.mimeType, type, onProgress)
    }

    /**
     * Upload a video with optional thumbnail generation.
     */
    suspend fun uploadVideo(
        uri: Uri,
        type: MediaUploadType,
        onProgress: ((Float) -> Unit)? = null
    ): Result<MediaUploadResult> {
        val processedResult = videoProcessor.processVideo(uri)
        if (processedResult.isFailure) return Result.failure(processedResult.exceptionOrNull()!!)

        val processed = processedResult.getOrNull()!!

        return uploadFile(processed.file, processed.mimeType, type, onProgress)
    }

    private suspend fun uploadFile(
        file: java.io.File,
        mimeType: String,
        type: MediaUploadType,
        onProgress: ((Float) -> Unit)? = null
    ): Result<MediaUploadResult> = suspendCancellableCoroutine { continuation ->
        // Map MediaUploadType to bucket name if needed
        val bucketName = when(type) {
            MediaUploadType.AVATAR -> "avatars"
            MediaUploadType.POST -> "posts"
            MediaUploadType.STORY -> "stories"
            MediaUploadType.REEL -> "reels"
            else -> null
        }

        storageService.uploadFile(
            filePath = file.absolutePath,
            bucketName = bucketName,
            callback = object : MediaStorageCallback {
                override fun onProgress(percent: Int) {
                    onProgress?.invoke(percent / 100f)
                }

                override fun onSuccess(url: String, publicId: String) {
                    continuation.resume(Result.success(MediaUploadResult(
                        url = url,
                        fileName = file.name,
                        fileSize = file.length(),
                        mimeType = mimeType,
                        publicId = publicId
                    )))
                }

                override fun onError(error: String) {
                    continuation.resume(Result.failure(Exception(error)))
                }
            }
        )
    }
}
