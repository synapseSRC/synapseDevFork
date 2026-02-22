package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.*
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    suspend fun getComments(postId: String, limit: Int = 20, offset: Int = 0): Result<List<Comment>>
    suspend fun addComment(postId: String, userId: String, text: String): Result<Comment>
    suspend fun deleteComment(commentId: String): Result<Unit>
    suspend fun toggleCommentReaction(commentId: String, userId: String, reactionType: ReactionType, oldReaction: ReactionType? = null): Result<Unit>
}
