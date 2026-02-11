package com.synapse.social.studioasinc.shared.data.source.remote

import com.synapse.social.studioasinc.shared.domain.model.StorageConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.datetime.Clock

class SupabaseUploadService(private val supabase: SupabaseClient) : UploadService {
    override suspend fun upload(
        fileBytes: ByteArray,
        fileName: String,
        config: StorageConfig,
        bucketName: String?,
        onProgress: (Float) -> Unit
    ): String {
        val targetBucket = bucketName ?: config.supabaseBucket
        val bucketToUse = if (targetBucket.isBlank()) "public" else targetBucket

        val bucket = supabase.storage.from(bucketToUse)
        val path = "${Clock.System.now().toEpochMilliseconds()}_$fileName"

        bucket.upload(path, fileBytes) {
            upsert = false
        }

        return bucket.publicUrl(path)
    }
}
