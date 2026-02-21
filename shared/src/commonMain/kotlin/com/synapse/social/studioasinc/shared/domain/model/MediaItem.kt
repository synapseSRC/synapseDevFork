package com.synapse.social.studioasinc.shared.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import com.synapse.social.studioasinc.shared.core.util.randomUUID



@Serializable
data class MediaItem(
    val id: String = randomUUID(),
    val url: String,
    val type: MediaType,
    val thumbnailUrl: String? = null,
    val duration: Long? = null,
    val size: Long? = null,
    val mimeType: String? = null,
    @Transient
    val likesCount: Int = 0,
    @Transient
    val commentsCount: Int = 0,
    @Transient
    val userHasLiked: Boolean = false
)



@Serializable
enum class MediaType {
    IMAGE,
    VIDEO
}
