package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model.models

/**
 * Represents metadata information about a media file.
 * Used for validation and processing decisions.
 */
data class MediaMetadata(
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
