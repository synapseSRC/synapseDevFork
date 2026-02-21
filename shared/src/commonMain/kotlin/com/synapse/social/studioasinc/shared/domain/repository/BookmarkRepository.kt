package com.synapse.social.studioasinc.shared.domain.repository

interface BookmarkRepository {
    suspend fun savePost(postId: String, userId: String): Result<Unit>
    suspend fun unsavePost(postId: String, userId: String): Result<Unit>
}
