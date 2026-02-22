package com.synapse.social.studioasinc.shared.domain.repository

import android.util.Log
import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.model.UserStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import kotlinx.coroutines.flow.Flow

interface PostDetailRepository {
    suspend fun getPostWithDetails(postId: String): Result<PostDetail>
    suspend fun incrementViewCount(postId: String): Result<Unit>
    fun observePostChanges(postId: String): Flow<PostDetail>
    suspend fun deletePost(postId: String): Result<Unit>
    fun hasYouTubeUrl(post: Post): Boolean
    fun isPostEdited(post: Post): Boolean
    fun getEditedTimestamp(post: Post): String?
    fun getAuthorBadge(author: UserProfile): String?
    fun isPostDataComplete(postDetail: PostDetail): Boolean
}
