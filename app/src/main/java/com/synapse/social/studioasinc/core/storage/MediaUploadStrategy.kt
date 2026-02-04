package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.core.storage

import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.local.database.StorageConfig
import java.io.File

/**
 * Interface defining a contract for file uploads.
 */
interface MediaUploadStrategy {
    suspend fun upload(
        file: File,
        config: StorageConfig,
        bucketName: String? = null,
        callback: MediaStorageService.UploadCallback
    )
}
