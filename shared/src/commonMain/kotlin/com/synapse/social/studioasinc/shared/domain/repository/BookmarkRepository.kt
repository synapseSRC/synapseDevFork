package com.synapse.social.studioasinc.shared.domain.repository

interface BookmarkRepository {
    suspend fun isBookmarked(postId: String): Result<Boolean>
    suspend fun toggleBookmark(postId: String, collectionId: String? = null): Result<Boolean>
}
