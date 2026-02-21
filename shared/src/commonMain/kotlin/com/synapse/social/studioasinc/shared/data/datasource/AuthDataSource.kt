package com.synapse.social.studioasinc.shared.data.datasource

import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.data.auth.IAuthenticationService
import com.synapse.social.studioasinc.shared.domain.model.OAuthProvider
import io.github.jan.supabase.auth.providers.OAuthProvider as SupabaseOAuthProvider
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Facebook

class AuthDataSource(
    private val authService: IAuthenticationService
) : IAuthDataSource {
    
    override suspend fun signUp(email: String, password: String): Result<String> {
        return authService.signUp(email, password)
    }
    
    override suspend fun signIn(email: String, password: String): Result<String> {
        return authService.signIn(email, password)
    }
    
    override suspend fun signInWithOAuth(provider: OAuthProvider, redirectUrl: String): Result<Unit> {
        val supabaseProvider = when (provider) {
            OAuthProvider.GOOGLE -> Google
            OAuthProvider.GITHUB -> Github
            OAuthProvider.APPLE -> Apple
            OAuthProvider.FACEBOOK -> Facebook
        }
        return authService.signInWithOAuth(supabaseProvider, redirectUrl)
    }
    
    override suspend fun signOut(): Result<Unit> {
        return authService.signOut()
    }
    
    override suspend fun getCurrentUserId(): String? {
        return authService.getCurrentUserId()
    }
    
    override suspend fun refreshSession(): Result<Unit> {
        return authService.refreshSession()
    }

    override suspend fun getOAuthUrl(provider: String, redirectUrl: String): Result<String> {
        return authService.getOAuthUrl(provider, redirectUrl)
    }

    override suspend fun handleOAuthCallback(code: String?, accessToken: String?, refreshToken: String?): Result<Unit> {
        return authService.handleOAuthCallback(code, accessToken, refreshToken)
    }

    override suspend fun getCurrentUserEmail(): String? {
        return authService.getCurrentUserEmail()
    }

    override suspend fun isEmailVerified(): Boolean {
        return authService.isEmailVerified()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authService.sendPasswordResetEmail(email)
    }

    override suspend fun resendVerificationEmail(email: String): Result<Unit> {
        return authService.resendVerificationEmail(email)
    }

    override suspend fun updatePassword(password: String): Result<Unit> {
        return authService.updatePassword(password)
    }
}
