package com.synapse.social.studioasinc.shared.domain.usecase.story

import com.synapse.social.studioasinc.shared.data.repository.StoryRepository
import javax.inject.Inject

class HasActiveStoryUseCase @Inject constructor(
    private val storyRepository: StoryRepository
) {
    suspend operator fun invoke(userId: String): Result<Boolean> {
        return storyRepository.hasActiveStory(userId)
    }
}
