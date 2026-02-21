package com.synapse.social.studioasinc.shared.data.local.database

data class CommentEntity(
    val id: String,
    val postId: String,
    val authorUid: String,
    val text: String,
    val timestamp: Long,
    val username: String? = null,
    val avatarUrl: String? = null,
    val parentCommentId: String? = null
)
