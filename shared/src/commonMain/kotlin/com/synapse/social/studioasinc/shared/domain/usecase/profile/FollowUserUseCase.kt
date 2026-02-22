package com.synapse.social.studioasinc.shared.domain.usecase.profile

import com.synapse.social.studioasinc.shared.domain.repository.ProfileRepository


class FollowUserUseCase constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(userId: String, targetUserId: String): Result<Unit> {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(targetUserId.isNotBlank()) { "Target user ID cannot be blank" }
        require(userId != targetUserId) { "Cannot follow yourself" }
        return repository.followUser(userId, targetUserId)
    }
}
