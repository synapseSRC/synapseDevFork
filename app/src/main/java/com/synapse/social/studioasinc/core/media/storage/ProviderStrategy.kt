package com.synapse.social.studioasinc.core.media.storage

import com.synapse.social.studioasinc.data.local.database.StorageConfig
import java.io.File

/**
 * Interface defining a contract for file uploads to various storage providers.
 */
interface ProviderStrategy {
    suspend fun upload(
        file: File,
        config: StorageConfig,
        bucketName: String? = null,
        callback: MediaStorageCallback
    )
}

/**
 * Callback for media storage operations.
 */
interface MediaStorageCallback {
    fun onProgress(percent: Int)
    fun onSuccess(url: String, publicId: String = "")
    fun onError(error: String)
}
