package com.synapse.social.studioasinc.shared.domain.usecase.auth
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.data.repository.AuthRepository
import com.synapse.social.studioasinc.shared.domain.model.OAuthDeepLink

class HandleOAuthCallbackUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(deepLink: OAuthDeepLink): Result<Unit> {
        if (deepLink.error != null) {
            return Result.failure(Exception("OAuth error: ${deepLink.errorDescription ?: deepLink.error}"))
        }
        return repository.handleOAuthCallback(deepLink.code, deepLink.accessToken, deepLink.refreshToken)
    }
}
