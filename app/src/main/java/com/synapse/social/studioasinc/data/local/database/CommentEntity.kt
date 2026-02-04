package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey
    val id: String,
    val postId: String,
    val authorUid: String,
    val text: String,
    val timestamp: Long,
    var username: String?,
    var avatarUrl: String?,
    @androidx.room.ColumnInfo(name = "parent_comment_id")
    val parentCommentId: String? = null
)
