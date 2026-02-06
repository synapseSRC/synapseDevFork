
package com.synapse.social.studioasinc.shared.data.source.remote

import com.synapse.social.studioasinc.shared.domain.model.StorageConfig

class R2UploadService : UploadService {
    override suspend fun upload(
        fileBytes: ByteArray,
        fileName: String,
        config: StorageConfig,
        bucketName: String?,
        onProgress: (Float) -> Unit
    ): String {
        throw NotImplementedError("Cloudflare R2 upload not yet implemented in Shared module")
    }
}
