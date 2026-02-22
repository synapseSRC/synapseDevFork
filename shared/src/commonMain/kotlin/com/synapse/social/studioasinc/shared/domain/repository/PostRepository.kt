package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.*
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    suspend fun createPost(post: Post): Result<Post>
    suspend fun getPost(postId: String): Result<Post?>
    suspend fun getPosts(limit: Int = 10, offset: Int = 0): Result<List<Post>>
    suspend fun getUserPosts(userId: String, limit: Int = 10, offset: Int = 0): Result<List<Post>>
    suspend fun updatePost(postId: String, updates: Map<String, Any?>): Result<Post>
    suspend fun updatePost(post: Post): Result<Post>
    suspend fun deletePost(postId: String): Result<Unit>
    suspend fun toggleReaction(postId: String, userId: String, reactionType: ReactionType, oldReaction: ReactionType? = null, skipCheck: Boolean = false): Result<Unit>
    suspend fun getReactionSummary(postId: String): Result<Map<ReactionType, Int>>
    suspend fun getUserReaction(postId: String, userId: String): Result<ReactionType?>
    suspend fun getUsersWhoReacted(postId: String, reactionType: ReactionType? = null): Result<List<UserReaction>>
    suspend fun toggleComments(postId: String): Result<Unit>
}
