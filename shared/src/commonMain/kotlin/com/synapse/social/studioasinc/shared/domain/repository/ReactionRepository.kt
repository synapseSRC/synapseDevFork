package com.synapse.social.studioasinc.shared.domain.repository

import android.util.Log
import com.synapse.social.studioasinc.shared.domain.model.CommentReaction
import com.synapse.social.studioasinc.shared.domain.model.CommentWithUser
import com.synapse.social.studioasinc.shared.domain.model.ReactionType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.coroutines.flow.Flow

interface ReactionRepository {
    suspend fun toggleReaction(
    suspend fun getReactionSummary(
    suspend fun getUserReaction(
    suspend fun populatePostReactions(posts: List<com.synapse.social.studioasinc.domain.model.Post>): List<com.synapse.social.studioasinc.domain.model.Post>
    suspend fun populateCommentReactions(comments: List<CommentWithUser>): List<CommentWithUser>
    suspend fun togglePostReaction(postId: String, reactionType: ReactionType, oldReaction: ReactionType?
    suspend fun toggleCommentReaction(commentId: String, reactionType: ReactionType, oldReaction: ReactionType?
    suspend fun getPostReactionSummary(postId: String)
    suspend fun getCommentReactionSummary(commentId: String)
    suspend fun getUserPostReaction(postId: String)
    suspend fun getUserCommentReaction(commentId: String)
    fun determineToggleResult(
    fun calculateReactionSummary(reactions: List<ReactionType>): Map<ReactionType, Int>
    fun isReactionSummaryAccurate(summary: Map<ReactionType, Int>, totalReactions: Int): Boolean
}
