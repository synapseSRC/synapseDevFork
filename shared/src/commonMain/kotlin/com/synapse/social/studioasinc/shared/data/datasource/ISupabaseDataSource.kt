package com.synapse.social.studioasinc.shared.data.datasource

import com.synapse.social.studioasinc.shared.domain.model.StorageConfig

interface ISupabaseDataSource {
    suspend fun uploadFile(fileBytes: ByteArray, fileName: String, bucketName: String?): Result<String>
    suspend fun deleteFile(fileName: String, bucketName: String): Result<Unit>
    suspend fun getStorageConfig(): Result<StorageConfig>
}
