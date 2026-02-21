package com.synapse.social.studioasinc.data.repository

import com.synapse.social.studioasinc.shared.data.local.entity.CommentEntity
import com.synapse.social.studioasinc.domain.model.Comment
import com.synapse.social.studioasinc.shared.data.database.Comment as DbComment

object CommentMapper {

    fun toSqlComment(comment: Comment, username: String? = null, avatarUrl: String? = null): DbComment {
        return DbComment(
            id = comment.key,
            postId = comment.postKey,
            authorId = comment.uid,
            text = comment.comment,
            timestamp = parsePushTime(comment.push_time),
            likesCount = 0,
            repliesCount = 0,
            isDeleted = false,
            parentCommentId = comment.replyCommentKey,
            username = username,
            avatarUrl = avatarUrl
        )
    }

    fun toSharedEntity(comment: Comment, username: String? = null, avatarUrl: String? = null): CommentEntity {
        return CommentEntity(
            id = comment.key,
            postId = comment.postKey,
            authorUid = comment.uid,
            text = comment.comment,
            timestamp = parsePushTime(comment.push_time),
            parentCommentId = comment.replyCommentKey,
            username = username,
            avatarUrl = avatarUrl
        )
    }

    // Kept for compatibility if needed, but preferably replace usage
    fun toEntity(comment: Comment, username: String? = null, avatarUrl: String? = null): DbComment {
        return toSqlComment(comment, username, avatarUrl)
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
