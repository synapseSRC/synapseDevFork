package com.synapse.social.studioasinc.shared.domain.usecase.auth
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.data.repository.AuthRepository
import io.github.jan.supabase.auth.providers.OAuthProvider

class SignInWithOAuthUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(provider: OAuthProvider, redirectUrl: String): Result<Unit> {
        return repository.signInWithOAuth(provider, redirectUrl)
    }
}
