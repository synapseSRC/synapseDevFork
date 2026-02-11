package com.synapse.social.studioasinc.shared.domain.usecase

import com.synapse.social.studioasinc.shared.data.repository.AuthRepository

class GetCurrentUserIdUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): String? {
        return authRepository.getCurrentUserId()
    }
}
