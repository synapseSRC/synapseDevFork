package com.synapse.social.studioasinc.shared.domain.usecase

import com.synapse.social.studioasinc.shared.domain.model.UserProfile
import com.synapse.social.studioasinc.shared.domain.repository.ProfileSectionsRepository
import kotlinx.coroutines.flow.Flow

class GetProfileUseCase(
    private val repository: ProfileSectionsRepository
) {
    operator fun invoke(userId: String): Flow<Result<UserProfile>> {
        return repository.getFullProfile(userId)
    }
}
