package com.synapse.social.studioasinc.core.storage

import android.content.Context
import com.synapse.social.studioasinc.core.media.storage.MediaStorageCallback
import com.synapse.social.studioasinc.core.media.storage.MediaStorageService
import com.synapse.social.studioasinc.data.local.database.AppSettingsManager
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

    interface UploadCallback {
        fun onUploadComplete(imageUrl: String)
        fun onUploadError(errorMessage: String)
    }

    /**
     * Upload an image using the configured storage provider.
     * @deprecated Use MediaFacade.uploadImage instead
     */
    fun uploadImage(context: Context, filePath: String, callback: UploadCallback) {
        val appSettingsManager = AppSettingsManager.getInstance(context)
        val mediaStorageService = MediaStorageService(context, appSettingsManager)

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
