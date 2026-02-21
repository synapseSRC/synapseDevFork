package com.synapse.social.studioasinc.shared.domain.usecase.profile
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.domain.model.UserProfile
import com.synapse.social.studioasinc.shared.domain.repository.ProfileRepository


class GetFollowingUseCase (private val repository: ProfileRepository) {
    suspend operator fun invoke(userId: String, limit: Int = 20, offset: Int = 0): Result<List<UserProfile>> {
        return repository.getFollowing(userId, limit, offset)
    }
}
