package com.synapse.social.studioasinc.core.storage

import android.content.Context
import com.synapse.social.studioasinc.core.media.processing.ImageCompressor
import com.synapse.social.studioasinc.core.storage.providers.CloudinaryProvider
import com.synapse.social.studioasinc.core.storage.providers.ImgBBProvider
import com.synapse.social.studioasinc.core.storage.providers.R2Provider
import com.synapse.social.studioasinc.core.storage.providers.SupabaseProvider
import com.synapse.social.studioasinc.data.local.database.AppSettingsManager
import com.synapse.social.studioasinc.data.local.database.StorageConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Unified media storage service that handles upload/download/link operations
 * for all media types with provider selection and fallback logic
 */
class MediaStorageService @Inject constructor(
    private val context: Context,
    private val appSettingsManager: AppSettingsManager,
    private val imageCompressor: ImageCompressor
) {

    enum class MediaType {
        PHOTO, VIDEO, OTHER
    }

    interface UploadCallback {
        fun onProgress(percent: Int)
        fun onSuccess(url: String, publicId: String = "")
        fun onError(error: String)
    }

    private val imgBBProvider = ImgBBProvider()
    private val cloudinaryProvider = CloudinaryProvider()
    private val supabaseProvider = SupabaseProvider()
    private val r2Provider = R2Provider()

    /**
     * Upload a file using the configured provider with fallback to default
     */
    suspend fun uploadFile(filePath: String, bucketName: String? = null, callback: UploadCallback) = withContext(Dispatchers.IO) {
        val originalFile = File(filePath)
        if (!originalFile.exists()) {
            callback.onError("File not found: $filePath")
            return@withContext
        }

        val mediaType = detectMediaType(originalFile)
        var fileToUpload = originalFile
        var isCompressed = false

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

        try {
            val config = appSettingsManager.storageConfigFlow.first()

            // Get selected provider or use fallback logic
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
                // Fallback to default if custom provider fails
                if (providerName != "Default") {
                    android.util.Log.w("MediaStorageService", "Provider $providerName failed, falling back to default: ${e.message}")
                    uploadWithDefaultProvider(fileToUpload, mediaType, config, callback)
                } else {
                    val errorMessage = when {
                    e is java.net.UnknownHostException || e is java.net.ConnectException -> "Network error: Check your connection"
                    e is java.net.SocketTimeoutException -> "Upload timed out"
                    e.message?.contains("403") == true -> "Permission denied by storage provider"
                    e.message?.contains("413") == true -> "File too large for this provider"
                    else -> e.message ?: "Unknown storage error"
                }
                callback.onError("Upload failed: $errorMessage")
                }
            }
        } finally {
            if (isCompressed && fileToUpload.exists()) {
                fileToUpload.delete()
                android.util.Log.d("MediaStorageService", "Temp compressed file deleted")
            }
        }
    }

    private fun getStrategy(providerName: String): MediaUploadStrategy? {
        return when (providerName) {
            "ImgBB" -> imgBBProvider
            "Cloudinary" -> cloudinaryProvider
            "Supabase" -> supabaseProvider
            "Cloudflare R2" -> r2Provider
            else -> null
        }
    }

    /**
     * Get the appropriate provider for a media type with fallback logic
     */
    private fun getProviderForMediaType(config: StorageConfig, mediaType: MediaType): String {
        val selectedProvider = when (mediaType) {
            MediaType.PHOTO -> config.photoProvider
            MediaType.VIDEO -> config.videoProvider
            MediaType.OTHER -> config.otherProvider
        }

        // If no provider selected or provider not configured, use Default
        return if (selectedProvider == null || !config.isProviderConfigured(selectedProvider)) {
            "Default"
        } else {
            selectedProvider
        }
    }

    /**
     * Upload using default app-provided credentials or custom if available
     */
    private suspend fun uploadWithDefaultProvider(file: File, mediaType: MediaType, config: StorageConfig, callback: UploadCallback) {
        when (mediaType) {
            MediaType.PHOTO -> {
                android.util.Log.d("MediaStorageService", "Using default provider (ImgBB) for photo")
                imgBBProvider.upload(file, config, null, callback)
            }
            MediaType.VIDEO, MediaType.OTHER -> {
                android.util.Log.d("MediaStorageService", "Using default provider (Cloudinary) for video/other")
                cloudinaryProvider.upload(file, config, null, callback)
            }
        }
    }

    /**
     * Detect media type from file extension
     */
    private fun detectMediaType(file: File): MediaType {
        val extension = file.extension.lowercase()
        return when {
            extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "avif") -> MediaType.PHOTO
            extension in listOf("mp4", "mov", "avi", "mkv", "webm", "3gp") -> MediaType.VIDEO
            else -> MediaType.OTHER
        }
    }
}
