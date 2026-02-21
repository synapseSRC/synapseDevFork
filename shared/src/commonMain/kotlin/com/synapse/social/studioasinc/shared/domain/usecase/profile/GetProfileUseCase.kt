package com.synapse.social.studioasinc.shared.domain.usecase.profile

import com.synapse.social.studioasinc.data.model.UserProfile
import com.synapse.social.studioasinc.shared.data.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(userId: String): Flow<Result<UserProfile>> {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        return repository.getProfile(userId)
    }
}
