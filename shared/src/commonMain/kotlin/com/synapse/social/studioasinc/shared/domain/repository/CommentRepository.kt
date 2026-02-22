package com.synapse.social.studioasinc.shared.domain.repository

import android.util.Log
import com.synapse.social.studioasinc.data.repository.CommentMapper
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.data.local.database.CommentDao
import com.synapse.social.studioasinc.shared.data.local.entity.CommentEntity
import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.model.UserStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun getComments(postId: String): Flow<Result<List<Comment>>>
    suspend fun fetchComments(postId: String, limit: Int
    suspend fun refreshComments(postId: String, limit: Int
    suspend fun getReplies(commentId: String): Result<List<CommentWithUser>>
    fun getCommentsForPost(postId: String): Flow<List<CommentWithUser>>
    suspend fun createComment(postId: String, content: String, mediaUrl: String?
    suspend fun addComment(postId: String, content: String, parentCommentId: String?
    suspend fun deleteComment(commentId: String): Result<Unit>
    suspend fun editComment(commentId: String, content: String): Result<CommentWithUser>
    suspend fun updateComment(commentId: String, newContent: String): Result<CommentWithUser>
    suspend fun pinComment(commentId: String): Result<Unit>
    suspend fun hideComment(commentId: String): Result<Unit>
    suspend fun reportComment(commentId: String, reason: String): Result<Unit>
}
