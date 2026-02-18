package com.synapse.social.studioasinc.data.repository

import com.synapse.social.studioasinc.shared.data.database.Comment as DbComment
import com.synapse.social.studioasinc.domain.model.Comment

object CommentMapper {

    fun toEntity(comment: Comment, username: String? = null, avatarUrl: String? = null): DbComment {
        return DbComment(
            id = comment.key,
            postId = comment.postKey,
            authorId = comment.uid,
            text = comment.comment,
            timestamp = parsePushTime(comment.push_time),
            likesCount = 0, // Default as domain model doesn't have it
            repliesCount = 0,
            isDeleted = false,
            parentCommentId = comment.replyCommentKey,
            username = username,
            avatarUrl = avatarUrl
        )
    }

    fun toModel(entity: DbComment): Comment {
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
