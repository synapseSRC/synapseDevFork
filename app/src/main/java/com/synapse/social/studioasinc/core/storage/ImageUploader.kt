package com.synapse.social.studioasinc.core.storage

import android.content.Context
import com.synapse.social.studioasinc.core.media.processing.ImageCompressor
import com.synapse.social.studioasinc.core.media.storage.MediaStorageCallback
import com.synapse.social.studioasinc.core.media.storage.MediaStorageService
import com.synapse.social.studioasinc.data.local.database.AppSettingsManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @deprecated Use MediaFacade.uploadImage() instead.
 */
@Deprecated(
    message = "Use MediaFacade instead",
    replaceWith = ReplaceWith(
        "mediaFacade.uploadImage(uri, MediaUploadType.POST)",
        "com.synapse.social.studioasinc.core.media.MediaFacade"
    )
)
object ImageUploader {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ImageUploaderEntryPoint {
        fun imageCompressor(): ImageCompressor
        fun mediaStorageService(): MediaStorageService
    }

    private var cachedEntryPoint: ImageUploaderEntryPoint? = null

    private fun getEntryPoint(context: Context): ImageUploaderEntryPoint {
        return cachedEntryPoint ?: EntryPointAccessors.fromApplication(
            context.applicationContext,
            ImageUploaderEntryPoint::class.java
        ).also { cachedEntryPoint = it }
    }

    interface UploadCallback {
        fun onUploadComplete(imageUrl: String)
        fun onUploadError(errorMessage: String)
    }

    /**
     * Upload an image using the configured storage provider.
     * @deprecated Use MediaFacade.uploadImage instead
     */
    fun uploadImage(context: Context, filePath: String, callback: UploadCallback) {
        val entryPoint = getEntryPoint(context)
        val mediaStorageService = entryPoint.mediaStorageService()

        CoroutineScope(Dispatchers.IO).launch {
            mediaStorageService.uploadFile(
                filePath = filePath,
                bucketName = null,
                callback = object : MediaStorageCallback {
                    override fun onProgress(percent: Int) {}

                    override fun onSuccess(url: String, publicId: String) {
                        callback.onUploadComplete(url)
                    }

                    override fun onError(error: String) {
                        callback.onUploadError(error)
                    }
                }
            )
        }
    }
}
