package com.synapse.social.studioasinc.data.repository

import com.synapse.social.studioasinc.shared.data.repository.AuthRepository as SharedAuthRepository
import io.github.jan.supabase.auth.providers.OAuthProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class AuthRepository @Inject constructor(
    private val sharedAuthRepository: SharedAuthRepository
) {



    suspend fun signUp(email: String, password: String): Result<String> {
        return sharedAuthRepository.signUp(email, password)
    }



    suspend fun signUpWithProfile(email: String, password: String, username: String): Result<String> {
        return sharedAuthRepository.signUpWithProfile(email, password, username)
    }



    suspend fun signIn(email: String, password: String): Result<String> {
        return sharedAuthRepository.signIn(email, password)
    }



    suspend fun signOut(): Result<Unit> {
        return sharedAuthRepository.signOut()
    }



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


    suspend fun getOAuthUrl(provider: String, redirectUrl: String): Result<String> {
        return sharedAuthRepository.getOAuthUrl(provider, redirectUrl)
    }

    suspend fun handleOAuthCallback(code: String?, accessToken: String?, refreshToken: String?): Result<Unit> {
        return sharedAuthRepository.handleOAuthCallback(code, accessToken, refreshToken)
    }

    suspend fun signInWithOAuth(provider: OAuthProvider, redirectUrl: String): Result<Unit> {
        return sharedAuthRepository.signInWithOAuth(provider, redirectUrl)
    }


    suspend fun getCurrentUserUid(): String? = getCurrentUserId()

    suspend fun ensureProfileExists(userId: String, email: String): Result<Unit> {
        return sharedAuthRepository.ensureProfileExists(userId, email)
    }
}
