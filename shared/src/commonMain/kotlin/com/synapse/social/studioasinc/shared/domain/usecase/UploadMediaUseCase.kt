package com.synapse.social.studioasinc.shared.domain.usecase

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

            var providerToUse = if (provider == StorageProvider.DEFAULT) {
                 when (mediaType) {
                     MediaType.PHOTO, MediaType.IMAGE -> StorageProvider.IMGBB
                     else -> StorageProvider.CLOUDINARY
                 }
            } else {
                 provider
            }

            if (provider != StorageProvider.DEFAULT && !config.isProviderConfigured(provider)) {
                 return Result.failure(Exception("Provider $provider selected but not configured."))
            }

            if (provider == StorageProvider.DEFAULT && !config.isProviderConfigured(providerToUse)) {
                 if (config.isProviderConfigured(StorageProvider.SUPABASE)) {
                     providerToUse = StorageProvider.SUPABASE
                 } else {
                     return Result.failure(Exception("Default provider ($providerToUse) is not configured."))
                 }
            }

            val service = getUploadService(providerToUse)
            val fileBytes = fileUploader.readFile(filePath)
            val fileName = fileUploader.getFileName(filePath).ifBlank { "upload_${TimeProvider.nowMillis()}" }

            val url = service.upload(fileBytes, fileName, config, bucketName, onProgress)
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
