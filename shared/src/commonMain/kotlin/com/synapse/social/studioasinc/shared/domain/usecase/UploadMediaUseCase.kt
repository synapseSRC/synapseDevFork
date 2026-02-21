package com.synapse.social.studioasinc.shared.domain.usecase
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.data.FileUploader
import com.synapse.social.studioasinc.shared.data.source.remote.CloudinaryUploadService
import com.synapse.social.studioasinc.shared.data.source.remote.ImgBBUploadService
import com.synapse.social.studioasinc.shared.data.source.remote.R2UploadService
import com.synapse.social.studioasinc.shared.data.source.remote.SupabaseUploadService
import com.synapse.social.studioasinc.shared.data.source.remote.UploadService
import com.synapse.social.studioasinc.shared.domain.model.MediaType
import com.synapse.social.studioasinc.shared.domain.model.StorageConfig
import com.synapse.social.studioasinc.shared.domain.model.StorageProvider
import com.synapse.social.studioasinc.shared.domain.repository.StorageRepository
import com.synapse.social.studioasinc.shared.util.TimeProvider
import kotlinx.coroutines.flow.first

class UploadMediaUseCase(
    private val repository: StorageRepository,
    private val fileUploader: FileUploader,
    private val imgBBUploadService: ImgBBUploadService,
    private val cloudinaryUploadService: CloudinaryUploadService,
    private val supabaseUploadService: SupabaseUploadService,
    private val r2UploadService: R2UploadService
) {
    suspend operator fun invoke(
        filePath: String,
        mediaType: MediaType,
        bucketName: String? = null,
        onProgress: (Float) -> Unit
    ): Result<String> {
        return try {
            val config = repository.getStorageConfig().first()
            val provider = getProviderForMediaType(config, mediaType)
            val fileBytes = fileUploader.readFile(filePath)
            val fileName = fileUploader.getFileName(filePath).ifBlank { "upload_${TimeProvider.nowMillis()}" }

            val providersToTry = getProvidersToTry(
                selectedProvider = provider,
                config = config,
                mediaType = mediaType
            )

            if (providersToTry.isEmpty()) {
                return Result.failure(
                    IllegalStateException(
                        "No configured storage provider available for $mediaType upload. " +
                            "Please configure at least one provider in Storage settings."
                    )
                )
            }

            val failures = mutableListOf<String>()
            providersToTry.forEach { providerToUse ->
                val service = getUploadService(providerToUse)
                runCatching {
                    service.upload(fileBytes, fileName, config, bucketName, onProgress)
                }.onSuccess { url ->
                    return Result.success(url)
                }.onFailure { throwable ->
                    failures += "$providerToUse: ${throwable.message ?: "Unknown error"}"
                }
            }

            Result.failure(
                Exception(
                    "Media upload failed for all configured providers. " +
                        failures.joinToString(separator = " | ")
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getProvidersToTry(
        selectedProvider: StorageProvider,
        config: StorageConfig,
        mediaType: MediaType
    ): List<StorageProvider> {
        if (selectedProvider != StorageProvider.DEFAULT) {
            return if (config.isProviderConfigured(selectedProvider)) {
                listOf(selectedProvider)
            } else {
                emptyList()
            }
        }

        val preferredOrder = when (mediaType) {
            MediaType.PHOTO, MediaType.IMAGE -> listOf(
                StorageProvider.IMGBB,
                StorageProvider.CLOUDINARY,
                StorageProvider.SUPABASE,
                StorageProvider.CLOUDFLARE_R2
            )

            MediaType.VIDEO, MediaType.AUDIO, MediaType.OTHER -> listOf(
                StorageProvider.CLOUDINARY,
                StorageProvider.SUPABASE,
                StorageProvider.CLOUDFLARE_R2,
                StorageProvider.IMGBB
            )
        }

        return preferredOrder.filter(config::isProviderConfigured)
    }

    private fun getProviderForMediaType(config: StorageConfig, mediaType: MediaType): StorageProvider {
        return when (mediaType) {
            MediaType.PHOTO, MediaType.IMAGE -> config.photoProvider
            MediaType.VIDEO -> config.videoProvider
            MediaType.OTHER -> config.otherProvider
            else -> config.otherProvider
        }
    }

    private fun getUploadService(provider: StorageProvider): UploadService {
        return when (provider) {
            StorageProvider.DEFAULT -> throw IllegalStateException("Default provider should have been resolved")
            StorageProvider.IMGBB -> imgBBUploadService
            StorageProvider.CLOUDINARY -> cloudinaryUploadService
            StorageProvider.SUPABASE -> supabaseUploadService
            StorageProvider.CLOUDFLARE_R2 -> r2UploadService
        }
    }
}
