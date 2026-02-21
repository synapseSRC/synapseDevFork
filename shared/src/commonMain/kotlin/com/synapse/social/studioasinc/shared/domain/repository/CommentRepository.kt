package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.*

interface CommentRepository {
    suspend fun fetchComments(postId: String, limit: Int = 50, offset: Int = 0): Result<List<CommentWithUser>>
    suspend fun addComment(postId: String, content: String, parentCommentId: String? = null): Result<CommentWithUser>
    suspend fun deleteComment(commentId: String): Result<Unit>
    suspend fun updateComment(commentId: String, newContent: String): Result<CommentWithUser>
}
