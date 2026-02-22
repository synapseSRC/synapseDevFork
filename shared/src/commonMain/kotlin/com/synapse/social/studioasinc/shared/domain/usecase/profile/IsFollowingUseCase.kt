package com.synapse.social.studioasinc.shared.domain.usecase.profile

import com.synapse.social.studioasinc.shared.domain.repository.ProfileRepository


class IsFollowingUseCase constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(userId: String, targetUserId: String): Result<Boolean> {
        if (userId.isBlank() || targetUserId.isBlank()) return Result.success(false)
        if (userId == targetUserId) return Result.success(false)
        return repository.isFollowing(userId, targetUserId)
    }
}
