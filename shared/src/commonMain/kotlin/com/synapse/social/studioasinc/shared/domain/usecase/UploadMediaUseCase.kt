package com.synapse.social.studioasinc.shared.domain.usecase
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.data.FileUploader
import com.synapse.social.studioasinc.shared.data.source.remote.UploadService
import com.synapse.social.studioasinc.shared.domain.model.MediaType
import com.synapse.social.studioasinc.shared.domain.model.StorageConfig
import com.synapse.social.studioasinc.shared.domain.model.StorageProvider
import com.synapse.social.studioasinc.shared.domain.repository.StorageRepository
import com.synapse.social.studioasinc.shared.util.TimeProvider
import kotlinx.coroutines.flow.first

class UploadMediaUseCase(
    private val repository: StorageRepository,
    private val fileUploader: FileUploader
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

            repository.uploadFile(fileBytes, fileName, provider, bucketName, onProgress)
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
}
