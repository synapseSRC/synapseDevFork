package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.repository

import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.local.database.CommentEntity
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model.Comment

object CommentMapper {

    fun toEntity(comment: Comment, username: String? = null, avatarUrl: String? = null): CommentEntity {
        return CommentEntity(
            id = comment.key,
            postId = comment.postKey,
            authorUid = comment.uid,
            text = comment.comment,
            timestamp = parsePushTime(comment.push_time),
            username = username,
            avatarUrl = avatarUrl,
            parentCommentId = comment.replyCommentKey
        )
    }

    fun toModel(entity: CommentEntity): Comment {
        return Comment(
            key = entity.id,
            postKey = entity.postId,
            uid = entity.authorUid,
            comment = entity.text,
            push_time = entity.timestamp.toString(),
            replyCommentKey = entity.parentCommentId
        )
    }

    private fun parsePushTime(pushTime: String): Long {
        return try {
            pushTime.toLongOrNull() ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
