package com.synapse.social.studioasinc.shared.domain.usecase.profile

import com.synapse.social.studioasinc.shared.data.model.UserProfile
import com.synapse.social.studioasinc.shared.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class GetProfileUseCase constructor(private val repository: ProfileRepository) {
    operator fun invoke(userId: String): Flow<Result<UserProfile>> {
        return repository.getProfile(userId)
    }
}
