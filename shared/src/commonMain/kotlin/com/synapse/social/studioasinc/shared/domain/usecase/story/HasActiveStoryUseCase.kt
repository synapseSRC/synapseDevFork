package com.synapse.social.studioasinc.shared.domain.usecase.story

import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HasActiveStoryUseCase(private val repository: StoryRepository) {
    suspend operator fun invoke(userId: String): Result<Boolean> {
        return repository.hasActiveStory(userId)
    }
}
