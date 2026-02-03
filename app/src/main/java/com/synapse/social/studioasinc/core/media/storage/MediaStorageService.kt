package com.synapse.social.studioasinc.core.media.storage

import android.content.Context
import com.synapse.social.studioasinc.core.media.processing.ImageCompressor
import com.synapse.social.studioasinc.core.media.storage.providers.CloudinaryProvider
import com.synapse.social.studioasinc.core.media.storage.providers.ImgBBProvider
import com.synapse.social.studioasinc.core.media.storage.providers.R2Provider
import com.synapse.social.studioasinc.core.media.storage.providers.SupabaseProvider
import com.synapse.social.studioasinc.data.local.database.AppSettingsManager
import com.synapse.social.studioasinc.data.local.database.StorageConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified media storage service that handles upload operations
 * for all media types with provider selection and fallback logic.
 */
@Singleton
class MediaStorageService @Inject constructor(
    private val context: Context,
    private val appSettingsManager: AppSettingsManager,
    private val imageCompressor: ImageCompressor
) {

    private val imgBBProvider = ImgBBProvider()
    private val cloudinaryProvider = CloudinaryProvider()
    private val supabaseProvider = SupabaseProvider()
    private val r2Provider = R2Provider()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Upload a file using the configured provider with fallback to default.
     */
    fun uploadFile(filePath: String, bucketName: String? = null, callback: MediaStorageCallback) {
        val originalFile = File(filePath)
        if (!originalFile.exists()) {
            callback.onError("File not found: $filePath")
            return
        }

        val mediaType = detectMediaType(originalFile)

        serviceScope.launch {
            var fileToUpload = originalFile
            var isCompressed = false

            try {
                // Compress if it's a photo
                if (mediaType == MediaType.PHOTO) {
                    val result = imageCompressor.compressFile(originalFile)
                    if (result.isSuccess) {
                        fileToUpload = result.getOrNull() ?: originalFile
                        isCompressed = (fileToUpload != originalFile)
                        android.util.Log.d("MediaStorageService", "Image compressed: ${originalFile.length()} -> ${fileToUpload.length()} bytes")
                    } else {
                        android.util.Log.w("MediaStorageService", "Compression failed, uploading original: ${result.exceptionOrNull()?.message}")
                    }
                }

                val config = appSettingsManager.storageConfigFlow.first()
                val providerName = getProviderForMediaType(config, mediaType)

                try {
                    if (providerName == "Default") {
                        uploadWithDefaultProvider(fileToUpload, mediaType, config, callback)
                    } else {
                        val strategy = getStrategy(providerName)
                        if (strategy != null) {
                            strategy.upload(fileToUpload, config, bucketName, callback)
                        } else {
                            callback.onError("Unknown provider: $providerName")
                        }
                    }
                } catch (e: Exception) {
                    if (providerName != "Default") {
                        android.util.Log.w("MediaStorageService", "Provider $providerName failed, falling back to default: ${e.message}")
                        uploadWithDefaultProvider(fileToUpload, mediaType, config, callback)
                    } else {
                        val errorMessage = when {
                            e is java.net.UnknownHostException || e is java.net.ConnectException -> "Network error: Check your connection"
                            e is java.net.SocketTimeoutException -> "Upload timed out"
                            e.message?.contains("403") == true -> "Permission denied by storage provider"
                            e.message?.contains("413") == true -> "File too large for this provider"
                            else -> "An unexpected storage error occurred. Please try again."
                        }
                        callback.onError("Upload failed: $errorMessage")
                    }
                }
            } catch (e: Exception) {
                callback.onError("Failed to get storage config: ${e.message}")
            } finally {
                if (isCompressed && fileToUpload.exists()) {
                    fileToUpload.delete()
                    android.util.Log.d("MediaStorageService", "Temp compressed file deleted")
                }
            }
        }
    }

    /**
     * Legacy method for backward compatibility with UploadCallback
     */
    fun uploadFile(filePath: String, bucketName: String? = null, callback: UploadCallback) {
        uploadFile(filePath, bucketName, object : MediaStorageCallback {
            override fun onProgress(percent: Int) = callback.onProgress(percent)
            override fun onSuccess(url: String, publicId: String) = callback.onSuccess(url, publicId)
            override fun onError(error: String) = callback.onError(error)
        })
    }

    private fun getStrategy(providerName: String): ProviderStrategy? {
        return when (providerName) {
            "ImgBB" -> imgBBProvider
            "Cloudinary" -> cloudinaryProvider
            "Supabase" -> supabaseProvider
            "Cloudflare R2" -> r2Provider
            else -> null
        }
    }

    private fun getProviderForMediaType(config: StorageConfig, mediaType: MediaType): String {
        val selectedProvider = when (mediaType) {
            MediaType.PHOTO -> config.photoProvider
            MediaType.VIDEO -> config.videoProvider
            MediaType.OTHER -> config.otherProvider
        }
        return if (selectedProvider == null || !config.isProviderConfigured(selectedProvider)) {
            "Default"
        } else {
            selectedProvider
        }
    }

    private suspend fun uploadWithDefaultProvider(file: File, mediaType: MediaType, config: StorageConfig, callback: MediaStorageCallback) {
        when (mediaType) {
            MediaType.PHOTO -> imgBBProvider.upload(file, config, null, callback)
            MediaType.VIDEO, MediaType.OTHER -> cloudinaryProvider.upload(file, config, null, callback)
        }
    }

    private fun detectMediaType(file: File): MediaType {
        val extension = file.extension.lowercase()
        return when {
            extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "avif") -> MediaType.PHOTO
            extension in listOf("mp4", "mov", "avi", "mkv", "webm", "3gp") -> MediaType.VIDEO
            else -> MediaType.OTHER
        }
    }

    enum class MediaType {
        PHOTO, VIDEO, OTHER
    }

    /**
     * Legacy callback interface for backward compatibility
     */
    interface UploadCallback {
        fun onProgress(percent: Int)
        fun onSuccess(url: String, publicId: String = "")
        fun onError(error: String)
    }
}
