package com.synapse.social.studioasinc.core.media.models

/**
 * Represents the result of a successful media upload operation.
 */
data class MediaUploadResult(
    val url: String,
    val thumbnailUrl: String? = null,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Long? = null,
    val publicId: String? = null
)
