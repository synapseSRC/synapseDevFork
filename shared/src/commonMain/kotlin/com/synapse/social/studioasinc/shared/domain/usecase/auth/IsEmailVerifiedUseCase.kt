package com.synapse.social.studioasinc.shared.domain.usecase.auth
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.data.repository.AuthRepository

class IsEmailVerifiedUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Boolean {
        return repository.isEmailVerified()
    }
}
