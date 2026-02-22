package com.synapse.social.studioasinc.shared.domain.repository

import android.content.SharedPreferences
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.synapse.social.studioasinc.core.network.SupabaseClient
import com.synapse.social.studioasinc.data.paging.PostPagingSource
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.domain.model.Post
import com.synapse.social.studioasinc.shared.domain.model.ReactionType
import com.synapse.social.studioasinc.shared.domain.model.UserReaction
import io.github.jan.supabase.SupabaseClient as JanSupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.*
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getPostsPaged(): Flow<PagingData<Post>>
    fun isExpired()
    fun constructMediaUrl(storagePath: String): String
    suspend fun createPost(post: Post): Result<Post>
    suspend fun getPost(postId: String): Result<Post?>
    fun getPosts(): Flow<Result<List<Post>>>
    fun getReelsPaged(): Flow<PagingData<Post>>
    suspend fun refreshPosts(page: Int, pageSize: Int): Result<Unit>
    suspend fun getUserPosts(userId: String): Result<List<Post>>
    suspend fun updatePost(postId: String, updates: Map<String, Any?>): Result<Post>
    suspend fun updatePost(post: Post): Result<Post>
    suspend fun deletePost(postId: String): Result<Unit>
    suspend fun toggleReaction(
    suspend fun getReactionSummary(postId: String): Result<Map<ReactionType, Int>>
    suspend fun getUserReaction(postId: String, userId: String): Result<ReactionType?>
    suspend fun getUsersWhoReacted(
    suspend fun toggleComments(postId: String): Result<Unit>
}
