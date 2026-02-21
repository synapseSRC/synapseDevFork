package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.*

interface PostRepository {
    suspend fun deletePost(postId: String): Result<Unit>
    suspend fun toggleComments(postId: String): Result<Unit>
    suspend fun getPost(postId: String): Result<Post?>
    suspend fun createPost(post: Post): Result<Post>
    suspend fun getUserPosts(userId: String): Result<List<Post>>
}
