package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.data.datasource.IAuthDataSource
import com.synapse.social.studioasinc.shared.domain.model.OAuthProvider
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AuthRepository(private val authDataSource: IAuthDataSource) {
    val sessionStatus: Flow<SessionStatus> get() = flowOf() // Placeholder
    private val TAG = "AuthRepository"

    suspend fun signUp(email: String, password: String): Result<String> {
        return authDataSource.signUp(email, password)
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        return authDataSource.signIn(email, password)
    }

    suspend fun signInWithOAuth(provider: OAuthProvider, redirectUrl: String): Result<Unit> {
        return authDataSource.signInWithOAuth(provider, redirectUrl)
    }

    suspend fun signOut(): Result<Unit> {
        return authDataSource.signOut()
    }

    suspend fun getCurrentUserId(): String? {
        return authDataSource.getCurrentUserId()
    }

    suspend fun refreshSession(): Result<Unit> {
        return authDataSource.refreshSession()
    }

    suspend fun getOAuthUrl(provider: String, redirectUrl: String): Result<String> {
        return authDataSource.getOAuthUrl(provider, redirectUrl)
    }

    suspend fun handleOAuthCallback(code: String?, accessToken: String?, refreshToken: String?): Result<Unit> {
        return authDataSource.handleOAuthCallback(code, accessToken, refreshToken)
    }

    suspend fun getCurrentUserEmail(): String? {
        return authDataSource.getCurrentUserEmail()
    }

    suspend fun isEmailVerified(): Boolean {
        return authDataSource.isEmailVerified()
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authDataSource.sendPasswordResetEmail(email)
    }

    suspend fun resendVerificationEmail(email: String): Result<Unit> {
        return authDataSource.resendVerificationEmail(email)
    }

    suspend fun updatePassword(password: String): Result<Unit> {
        return authDataSource.updatePassword(password)
    }

    suspend fun getCurrentUserUid(): String? = getCurrentUserId()
}

