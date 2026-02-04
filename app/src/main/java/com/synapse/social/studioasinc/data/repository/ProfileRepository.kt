package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.repository

import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.model.UserProfile
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.feature.profile.profile.components.MediaItem
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(userId: String): Flow<Result<UserProfile>>
    suspend fun updateProfile(userId: String, profile: UserProfile): Result<UserProfile>
    suspend fun followUser(userId: String, targetUserId: String): Result<Unit>
    suspend fun unfollowUser(userId: String, targetUserId: String): Result<Unit>
    suspend fun getFollowers(userId: String, limit: Int = 20, offset: Int = 0): Result<List<UserProfile>>
    suspend fun getFollowing(userId: String, limit: Int = 20, offset: Int = 0): Result<List<UserProfile>>
    suspend fun getProfilePosts(userId: String, limit: Int = 10, offset: Int = 0): Result<List<com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model.Post>>
    suspend fun getProfilePhotos(userId: String, limit: Int = 20, offset: Int = 0): Result<List<MediaItem>>
    suspend fun getProfileReels(userId: String, limit: Int = 20, offset: Int = 0): Result<List<MediaItem>>
    suspend fun isFollowing(userId: String, targetUserId: String): Result<Boolean>
}
