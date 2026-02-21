package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.*
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun getActiveStories(currentUserId: String): Flow<List<StoryWithUser>>
    suspend fun getUserStories(userId: String): Result<List<Story>>
    suspend fun deleteStory(storyId: String): Result<Unit>
    suspend fun markAsSeen(storyId: String, viewerId: String): Result<Unit>
    suspend fun hasSeenStory(storyId: String, viewerId: String): Result<Boolean>
    suspend fun hasActiveStory(userId: String): Result<Boolean>
}
