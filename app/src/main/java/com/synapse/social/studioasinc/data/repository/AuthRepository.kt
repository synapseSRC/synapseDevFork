package com.synapse.social.studioasinc.data.repository

import com.synapse.social.studioasinc.data.local.auth.TokenManager
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.shared.data.repository.AuthRepository as SharedAuthRepository
import io.github.jan.supabase.auth.providers.OAuthProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android AuthRepository - Simplified facade over shared module.
 * All business logic now delegated to shared AuthRepository.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val tokenManager: TokenManager, // Legacy TokenManager (keep for migration/compat)
    private val sharedAuthRepository: SharedAuthRepository // Shared Logic Delegate
) {

    /**
     * Register a new user. Delegates to Shared Module.
     */
    suspend fun signUp(email: String, password: String): Result<String> {
        return sharedAuthRepository.signUp(email, password)
    }

    /**
     * Create user account with profile. Delegates to Shared Module.
     */
    suspend fun signUpWithProfile(email: String, password: String, username: String): Result<String> {
        return sharedAuthRepository.signUpWithProfile(email, password, username)
    }

    /**
     * Sign in. Delegates to Shared Module.
     */
    suspend fun signIn(email: String, password: String): Result<String> {
        tokenManager.clearTokens()
        return sharedAuthRepository.signIn(email, password)
    }

    /**
     * Sign out. Delegates to Shared Module.
     */
    suspend fun signOut(): Result<Unit> {
        tokenManager.clearTokens()
        return sharedAuthRepository.signOut()
    }

    /**
     * Get current user ID. Delegates to Shared Module.
     */
    fun getCurrentUserId(): String? {
        return runBlocking { sharedAuthRepository.getCurrentUserId() }
    }

    fun getCurrentUserEmail(): String? {
        return runBlocking { sharedAuthRepository.getCurrentUserEmail() }
    }

    fun isEmailVerified(): Boolean {
        return runBlocking { sharedAuthRepository.isEmailVerified() }
    }

    suspend fun refreshSession(): Result<Unit> {
        return sharedAuthRepository.refreshSession()
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return sharedAuthRepository.sendPasswordResetEmail(email)
    }

    suspend fun resendVerificationEmail(email: String): Result<Unit> {
        return sharedAuthRepository.resendVerificationEmail(email)
    }

    suspend fun updateUserPassword(password: String): Result<Unit> {
        return sharedAuthRepository.updatePassword(password)
    }

    suspend fun restoreSession(): Boolean {
        return sharedAuthRepository.restoreSession()
    }

    fun isUserLoggedIn(): Boolean {
        return getCurrentUserId() != null
    }

    fun observeAuthState(): Flow<Boolean> {
        return flowOf(isUserLoggedIn())
    }

    // Platform-specific OAuth methods (if needed)
    suspend fun getOAuthUrl(provider: String, redirectUrl: String): Result<String> {
        return sharedAuthRepository.getOAuthUrl(provider, redirectUrl)
    }

    suspend fun handleOAuthCallback(code: String?, accessToken: String?, refreshToken: String?): Result<Unit> {
        return sharedAuthRepository.handleOAuthCallback(code, accessToken, refreshToken)
    }

    suspend fun signInWithOAuth(provider: OAuthProvider, redirectUrl: String): Result<Unit> {
        return sharedAuthRepository.signInWithOAuth(provider, redirectUrl)
    }

    // Legacy compatibility methods
    suspend fun getCurrentUserUid(): String? = getCurrentUserId()
    suspend fun recoverSession(accessToken: String): Result<Unit> = Result.success(Unit)
    suspend fun ensureProfileExistsPublic(userId: String, email: String) = Unit
}
