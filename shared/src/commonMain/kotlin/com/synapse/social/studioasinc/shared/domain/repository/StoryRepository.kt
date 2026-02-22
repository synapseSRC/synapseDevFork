package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.*
import kotlinx.coroutines.flow.Flow

interface StoryRepository {


    suspend fun hasActiveStory(userId: String): Result<Boolean>



    fun getActiveStories(currentUserId: String): Flow<List<StoryWithUser>>



    suspend fun getUserStories(userId: String): Result<List<Story>>



    suspend fun createStory(
        userId: String,
        mediaString: String,
        mediaType: StoryMediaType,
        privacy: StoryPrivacy,
        duration: Int = 5
    ): Result<Story>



    suspend fun deleteStory(storyId: String): Result<Unit>



    suspend fun markAsSeen(storyId: String, viewerId: String): Result<Unit>



    suspend fun getStoryViewers(storyId: String): Result<List<StoryViewWithUser>>



    suspend fun hasSeenStory(storyId: String, viewerId: String): Result<Boolean>
}
