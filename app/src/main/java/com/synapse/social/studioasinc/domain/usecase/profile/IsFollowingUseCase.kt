package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.usecase.profile

import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.repository.ProfileRepository
import javax.inject.Inject

class IsFollowingUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(userId: String, targetUserId: String): Result<Boolean> {
        if (userId.isBlank() || targetUserId.isBlank()) return Result.success(false)
        if (userId == targetUserId) return Result.success(false)
        return repository.isFollowing(userId, targetUserId)
    }
}
