package com.synapse.social.studioasinc.shared.domain.usecase.story
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.domain.repository.StoryRepository


class HasActiveStoryUseCase (
    private val storyRepository: StoryRepository
) {
    suspend operator fun invoke(userId: String): Result<Boolean> {
        return storyRepository.hasActiveStory(userId)
    }
}
