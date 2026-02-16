package com.synapse.social.studioasinc.shared.data.source.remote

import com.synapse.social.studioasinc.shared.domain.model.StorageConfig
import com.synapse.social.studioasinc.shared.util.TimeProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.github.aakira.napier.Napier

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

        Napier.d("Uploading to Supabase bucket: $bucketToUse, file: $fileName", tag = "SupabaseUpload")

        try {
            val bucket = supabase.storage.from(bucketToUse)
            val path = "${TimeProvider.nowMillis()}_$fileName"

            bucket.upload(path, fileBytes) {
                upsert = false
            }

            // Note: Progress tracking is not directly supported by current supabase-kt upload simple method easily
            // without custom content body, but upload is usually fast enough or handled by library internally if supported.
            // For now we just call onProgress(1.0f) at end.
            onProgress(1.0f)

            val publicUrl = bucket.publicUrl(path)
            Napier.d("Supabase upload successful: $publicUrl", tag = "SupabaseUpload")
            return publicUrl
        } catch (e: Exception) {
            Napier.e("Supabase upload failed to bucket: $bucketToUse", e, tag = "SupabaseUpload")
            throw e
        }
    }
}
