package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.*

interface ReactionRepository {
    suspend fun toggleReaction(postId: String, targetType: String, reactionType: ReactionType, oldReaction: ReactionType? = null, skipCheck: Boolean = false): Result<Unit>
    suspend fun getReactionSummary(postId: String, targetType: String): Result<Map<ReactionType, Int>>
    suspend fun getUserReaction(postId: String, targetType: String): Result<ReactionType?>
}
