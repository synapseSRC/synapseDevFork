package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.*

interface ReactionRepository {
    suspend fun toggleReaction(targetId: String, targetType: String, reactionType: ReactionType, oldReaction: ReactionType? = null, skipCheck: Boolean = false): Result<Unit>
    suspend fun getReactionSummary(targetId: String, targetType: String): Result<Map<ReactionType, Int>>
    suspend fun getUserReaction(targetId: String, targetType: String): Result<ReactionType?>
    suspend fun populatePostReactions(posts: List<Post>): List<Post>
}
