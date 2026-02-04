package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.data.auth.IAuthenticationService
import io.github.aakira.napier.Napier

/**
 * Shared authentication repository using service abstraction.
 */
class AuthRepository(
    private val authService: IAuthenticationService
) {
    private val TAG = "AuthRepository"

    suspend fun signUp(email: String, password: String): Result<String> {
        return authService.signUp(email, password)
    }

    suspend fun signUpWithProfile(email: String, password: String, username: String): Result<String> {
        return try {
            // 1. Create Auth User
            val signUpResult = authService.signUp(email, password)
            if (signUpResult.isFailure) return signUpResult

            val userId = signUpResult.getOrThrow()

            // 2. Create Profile
            val profileResult = authService.createUserProfile(userId, email, username)
            if (profileResult.isFailure) {
                Napier.e("Profile creation failed after successful signup", tag = TAG)
                return Result.failure(profileResult.exceptionOrNull() ?: Exception("Profile creation failed"))
            }

            authService.storeSessionTokens("", "", userId, email, 3600)
            Result.success(userId)
        } catch (e: Exception) {
            Napier.e("Sign up with profile failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        return authService.signIn(email, password)
    }

    suspend fun signOut(): Result<Unit> {
        return authService.signOut()
    }

    suspend fun getCurrentUserId(): String? {
        return authService.getCurrentUserId()
    }

    suspend fun getCurrentUserEmail(): String? {
        return authService.getCurrentUserEmail()
    }

    suspend fun isEmailVerified(): Boolean {
        return authService.isEmailVerified()
    }

    suspend fun restoreSession(): Boolean {
        return authService.restoreSession()
    }

    suspend fun refreshSession(): Result<Unit> {
        return authService.refreshSession()
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authService.sendPasswordResetEmail(email)
    }

    suspend fun resendVerificationEmail(email: String): Result<Unit> {
        return authService.resendVerificationEmail(email)
    }

    suspend fun updatePassword(password: String): Result<Unit> {
        return authService.updatePassword(password)
    }

    suspend fun updateEmail(email: String): Result<Unit> {
        return authService.updateEmail(email)
    }

    suspend fun getOAuthUrl(provider: String, redirectUrl: String): Result<String> {
        return authService.getOAuthUrl(provider, redirectUrl)
    }

    suspend fun handleOAuthCallback(code: String?, accessToken: String?, refreshToken: String?): Result<Unit> {
        return authService.handleOAuthCallback(code, accessToken, refreshToken)
    }
}
