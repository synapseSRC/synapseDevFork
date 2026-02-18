package com.synapse.social.studioasinc.data.repository

import com.synapse.social.studioasinc.shared.data.database.Comment as SharedComment
import com.synapse.social.studioasinc.domain.model.Comment

object CommentMapper {

    fun toEntity(comment: Comment, username: String? = null, avatarUrl: String? = null): SharedComment {
        return SharedComment(
            id = comment.key,
            postId = comment.postKey,
            authorId = comment.uid,
            text = comment.comment,
            timestamp = parsePushTime(comment.push_time),
            likesCount = 0, // Default
            repliesCount = 0, // Default
            isDeleted = false, // Default
            parentCommentId = comment.replyCommentKey
        )
    }

    fun toModel(entity: SharedComment): Comment {
        return Comment(
            key = entity.id,
            postKey = entity.postId,
            uid = entity.authorId,
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
