package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.data.mapper.PostMapper
import com.synapse.social.studioasinc.shared.data.model.*
import com.synapse.social.studioasinc.shared.core.network.SupabaseErrorHandler
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

class PostRepository(
    private val storageDatabase: StorageDatabase,
    private val client: SupabaseClient
) : com.synapse.social.studioasinc.shared.domain.repository.PostRepository {

    override suspend fun createPost(post: Post): Result<Post> = withContext(Dispatchers.IO) {
        try {
            val dto = PostMapper.toDto(post)
            client.from("posts").insert(dto) {
                select()
            }.decodeSingle<PostDto>()
            Result.success(post)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPost(postId: String): Result<Post?> = withContext(Dispatchers.IO) {
        try {
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserPosts(userId: String): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.from("posts").delete {
                filter { eq("id", postId) }
            }
            storageDatabase.postQueries.deleteById(postId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleComments(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Logic to toggle comments
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
