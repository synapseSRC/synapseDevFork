package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.ReactionType

interface PostInteractionRepository {
    suspend fun likePost(postId: String, userId: String): Result<Unit>
    suspend fun unlikePost(postId: String, userId: String): Result<Unit>
    suspend fun toggleReaction(postId: String, userId: String, reactionType: ReactionType, oldReaction: ReactionType? = null, skipCheck: Boolean = false): Result<Unit>
    suspend fun savePost(postId: String, userId: String): Result<Unit>
    suspend fun unsavePost(postId: String, userId: String): Result<Unit>
}
