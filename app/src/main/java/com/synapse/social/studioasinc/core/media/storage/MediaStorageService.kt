package com.synapse.social.studioasinc.core.media.storage

import android.content.Context
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
    private val appSettingsManager: AppSettingsManager
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
        val file = File(filePath)
        if (!file.exists()) {
            callback.onError("File not found: $filePath")
            return
        }

        val mediaType = detectMediaType(file)

        serviceScope.launch {
            try {
                val config = appSettingsManager.storageConfigFlow.first()
                val providerName = getProviderForMediaType(config, mediaType)

                try {
                    if (providerName == "Default") {
                        uploadWithDefaultProvider(file, mediaType, config, callback)
                    } else {
                        val strategy = getStrategy(providerName)
                        if (strategy != null) {
                            strategy.upload(file, config, bucketName, callback)
                        } else {
                            callback.onError("Unknown provider: $providerName")
                        }
                    }
                } catch (e: Exception) {
                    if (providerName != "Default") {
                        android.util.Log.w("MediaStorageService", "Provider $providerName failed, falling back to default: ${e.message}")
                        uploadWithDefaultProvider(file, mediaType, config, callback)
                    } else {
                        callback.onError("Upload failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                callback.onError("Failed to get storage config: ${e.message}")
            }
        }
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
}
