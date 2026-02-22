package com.synapse.social.studioasinc.shared.domain.usecase.auth
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.data.repository.AuthRepository

class RefreshSessionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.refreshSession()
    }
}
