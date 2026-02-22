package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.repository.ReactionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ReactionRepository(private val client: SupabaseClient) : ReactionRepository {
    override suspend fun toggleReaction(postId: String, targetType: String, reactionType: ReactionType, oldReaction: ReactionType?, skipCheck: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }

    override suspend fun getReactionSummary(postId: String, targetType: String): Result<Map<ReactionType, Int>> = withContext(Dispatchers.IO) {
        Result.success(emptyMap())
    }

    override suspend fun getUserReaction(postId: String, targetType: String): Result<ReactionType?> = withContext(Dispatchers.IO) {
        Result.success(null)
    }
}
