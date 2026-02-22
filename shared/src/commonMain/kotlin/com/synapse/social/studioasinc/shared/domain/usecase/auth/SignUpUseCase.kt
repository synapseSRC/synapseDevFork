package com.synapse.social.studioasinc.shared.domain.usecase.auth
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.data.repository.AuthRepository

class SignUpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, username: String): Result<String> {
        return repository.signUpWithProfile(email, password, username)
    }
}
