package com.synapse.social.studioasinc.shared.domain.usecase.profile
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.domain.repository.ProfileRepository


class IsFollowingUseCase (private val repository: ProfileRepository) {
    suspend operator fun invoke(targetUserId: String): Result<Boolean> {
        if (targetUserId.isBlank()) return Result.success(false)
        return repository.isFollowing(targetUserId)
    }
}
