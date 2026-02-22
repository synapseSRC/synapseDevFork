package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.core.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.flow.Flow

interface PostInteractionRepository {
    suspend fun likePost(postId: String, userId: String): Result<Unit>
    suspend fun unlikePost(postId: String, userId: String): Result<Unit>
    suspend fun savePost(postId: String, userId: String): Result<Unit>
    suspend fun unsavePost(postId: String, userId: String): Result<Unit>
    suspend fun deletePost(postId: String, userId: String): Result<Unit>
    suspend fun reportPost(postId: String, userId: String, reason: String): Result<Unit>
}
