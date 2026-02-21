package com.synapse.social.studioasinc.shared.domain.usecase.auth
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.data.repository.AuthRepository

class GetCurrentUserIdUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): String? {
        return repository.getCurrentUserId()
    }
}
