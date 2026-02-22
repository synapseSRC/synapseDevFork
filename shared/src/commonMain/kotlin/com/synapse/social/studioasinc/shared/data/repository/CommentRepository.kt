package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.repository.CommentRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class CommentRepository(
    private val storageDatabase: StorageDatabase,
    private val client: SupabaseClient
) : CommentRepository {

    override suspend fun fetchComments(postId: String, limit: Int, offset: Int): Result<List<CommentWithUser>> = withContext(Dispatchers.IO) {
        Result.success(emptyList())
    }

    override suspend fun addComment(postId: String, content: String, parentCommentId: String?): Result<CommentWithUser> = withContext(Dispatchers.IO) {
        Result.failure(Exception("Not implemented"))
    }

    override suspend fun deleteComment(commentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }

    override suspend fun updateComment(commentId: String, newContent: String): Result<CommentWithUser> = withContext(Dispatchers.IO) {
        Result.failure(Exception("Not implemented"))
    }
}
