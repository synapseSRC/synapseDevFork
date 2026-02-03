package com.synapse.social.studioasinc.core.media.models

/**
 * Represents the progress of a media upload operation.
 */
data class UploadProgress(
    val bytesTransferred: Long,
    val totalBytes: Long,
    val percentage: Float
)
