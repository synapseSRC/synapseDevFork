package com.synapse.social.studioasinc.shared.domain.usecase

import com.synapse.social.studioasinc.shared.domain.model.PrivacySettings
import com.synapse.social.studioasinc.shared.domain.repository.ProfileSectionsRepository
import kotlinx.coroutines.flow.Flow

class GetPrivacySettingsUseCase(
    private val repository: ProfileSectionsRepository
) {
    operator fun invoke(userId: String): Flow<Result<PrivacySettings>> {
        return repository.getPrivacySettings(userId)
    }
}

class UpdatePrivacySettingsUseCase(
    private val repository: ProfileSectionsRepository
) {
    suspend operator fun invoke(userId: String, settings: PrivacySettings): Result<Unit> {
        return repository.updatePrivacySettings(userId, settings)
    }
}
