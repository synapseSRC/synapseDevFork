package com.synapse.social.studioasinc.shared.data.auth

/**
 * Authentication service interface for BaaS abstraction.
 * Implementations handle platform-specific auth operations.
 */
interface IAuthenticationService {
    
    // Core Authentication
    suspend fun signUp(email: String, password: String): Result<String>
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun signOut(): Result<Unit>
    
    // Profile Management
    suspend fun createUserProfile(userId: String, email: String, username: String): Result<Unit>
    suspend fun ensureProfileExists(userId: String, email: String): Result<Unit>
    
    // Session Management
    suspend fun getCurrentUserId(): String?
    suspend fun getCurrentUserEmail(): String?
    suspend fun isEmailVerified(): Boolean
    suspend fun restoreSession(): Boolean
    suspend fun refreshSession(): Result<Unit>
    
    // Password & Email Operations
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun resendVerificationEmail(email: String): Result<Unit>
    suspend fun updatePassword(password: String): Result<Unit>
    suspend fun updateEmail(email: String): Result<Unit>
    
    // OAuth Operations
    suspend fun getOAuthUrl(provider: String, redirectUrl: String): Result<String>
    suspend fun handleOAuthCallback(code: String?, accessToken: String?, refreshToken: String?): Result<Unit>
    
    // Session Storage
    suspend fun storeSessionTokens(accessToken: String, refreshToken: String, userId: String, userEmail: String, expiresIn: Int)
    suspend fun clearStoredTokens()
}
