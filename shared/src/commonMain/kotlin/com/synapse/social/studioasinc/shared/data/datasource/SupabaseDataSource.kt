package com.synapse.social.studioasinc.shared.data.datasource

import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.domain.model.StorageConfig
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload

class SupabaseDataSource : ISupabaseDataSource {
    
    override suspend fun uploadFile(fileBytes: ByteArray, fileName: String, bucketName: String?): Result<String> {
        return try {
            val bucket = bucketName ?: "uploads"
            SupabaseClient.client.storage[bucket].upload(fileName, fileBytes)
            val url = SupabaseClient.client.storage[bucket].publicUrl(fileName)
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteFile(fileName: String, bucketName: String): Result<Unit> {
        return try {
            SupabaseClient.client.storage[bucketName].delete(fileName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getStorageConfig(): Result<StorageConfig> {
        // Implementation would fetch from Supabase settings table
        return Result.failure(NotImplementedError("Storage config retrieval not implemented"))
    }
}
