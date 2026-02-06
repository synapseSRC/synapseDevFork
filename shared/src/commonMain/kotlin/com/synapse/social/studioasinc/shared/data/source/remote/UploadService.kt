
package com.synapse.social.studioasinc.shared.data.source.remote

import com.synapse.social.studioasinc.shared.domain.model.StorageConfig

interface UploadService {
    suspend fun upload(
        fileBytes: ByteArray,
        fileName: String,
        config: StorageConfig,
        bucketName: String? = null,
        onProgress: (Float) -> Unit
    ): String
}
