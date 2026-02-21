package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.*

interface ProfileRepository {
    suspend fun archiveProfile(userId: String): Result<Unit>
    suspend fun blockUser(userId: String): Result<Unit>
    suspend fun followUser(userId: String): Result<Unit>
    suspend fun getFollowing(userId: String, limit: Int = 20, offset: Int = 0): Result<List<UserProfile>>
    suspend fun getProfilePosts(userId: String, limit: Int = 20, offset: Int = 0): Result<List<Post>>
    suspend fun getProfilePhotos(userId: String, limit: Int = 20, offset: Int = 0): Result<List<MediaItem>>
    suspend fun getProfileReels(userId: String, limit: Int = 20, offset: Int = 0): Result<List<Reel>>
    suspend fun getProfile(userId: String): Result<UserProfile?>
    suspend fun isFollowing(userId: String): Result<Boolean>
    suspend fun lockProfile(userId: String): Result<Unit>
    suspend fun muteUser(userId: String): Result<Unit>
    suspend fun reportUser(userId: String, reason: String): Result<Unit>
    suspend fun unfollowUser(userId: String): Result<Unit>
    suspend fun updateProfile(userId: String, profile: UserProfile): Result<UserProfile>
}
