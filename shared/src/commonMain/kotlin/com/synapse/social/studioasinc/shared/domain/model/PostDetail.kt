package com.synapse.social.studioasinc.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PostDetail(
    val post: Post,
    val comments: List<CommentWithUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val canShowMedia: Boolean get() = post.mediaItems?.isNotEmpty() ?: false
    val hasPoll: Boolean get() = post.hasPoll ?: false
    val hasLocation: Boolean get() = post.hasLocation ?: false
    val hasYoutubeUrl: Boolean get() = !post.youtubeUrl.isNullOrEmpty()
    val isEncrypted: Boolean get() = post.isEncrypted ?: false
    val isEdited: Boolean get() = post.isEdited ?: false
}
