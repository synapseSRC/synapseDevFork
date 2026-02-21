package com.synapse.social.studioasinc.shared.domain.usecase.profile
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.domain.repository.ProfileRepository


class FollowUserUseCase (private val repository: ProfileRepository) {
    suspend operator fun invoke(targetUserId: String): Result<Unit> {
        require(targetUserId.isNotBlank()) { "Target user ID cannot be blank" }
        return repository.followUser(targetUserId)
    }
}
