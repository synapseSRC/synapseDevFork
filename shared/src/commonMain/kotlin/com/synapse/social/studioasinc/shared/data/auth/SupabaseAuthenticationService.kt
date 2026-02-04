package com.synapse.social.studioasinc.shared.data.auth

import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.core.util.getCurrentIsoTime
import com.synapse.social.studioasinc.shared.data.model.UserProfileInsert
import com.synapse.social.studioasinc.shared.data.model.UserSettingsInsert
import com.synapse.social.studioasinc.shared.data.model.UserPresenceInsert
import io.github.aakira.napier.Napier
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OtpType
import kotlin.time.ExperimentalTime
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.delay

/**
 * Supabase implementation of authentication service.
 */
class SupabaseAuthenticationService(
    private val tokenManager: TokenManager
) : IAuthenticationService {
    
    private val client = SupabaseClient.client
    private val TAG = "SupabaseAuthService"
    
    override suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val user = client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val userId = user?.id ?: throw Exception("Failed to get user ID")
            Result.success(userId)
        } catch (e: Exception) {
            Napier.e("Sign up failed", e, tag = TAG)
            Result.failure(e)
        }
    }
    
    override suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Email and password cannot be empty"))
            }
            
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val userId = client.auth.currentUserOrNull()?.id
                ?: throw Exception("Failed to get user ID after signin")
            
            storeCurrentSessionTokens()
            ensureProfileExists(userId, email)
            
            Result.success(userId)
        } catch (e: Exception) {
            Napier.e("Sign in failed", e, tag = TAG)
            Result.failure(e)
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        return try {
            client.auth.signOut()
            clearStoredTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            clearStoredTokens() // Clear anyway
            Napier.e("Sign out failed", e, tag = TAG)
            Result.failure(e)
        }
    }
    
    override suspend fun createUserProfile(userId: String, email: String, username: String): Result<Unit> {
        return try {
            val userProfile = UserProfileInsert(
                username = username,
                email = email,
                created_at = getCurrentIsoTime(),
                updated_at = getCurrentIsoTime(),
                join_date = getCurrentIsoTime(),
                account_premium = false,
                verify = false,
                banned = false,
                followers_count = 0,
                following_count = 0,
                posts_count = 0,
                user_level_xp = 500,
                status = "offline"
            )
            
            client.from("users").insert(userProfile)
            
            // Create settings/presence (best effort)
            try {
                client.from("user_settings").insert(UserSettingsInsert(user_id = userId))
            } catch (e: Exception) {
                Napier.w("user_settings creation failed", e, tag = TAG)
            }
            
            try {
                client.from("user_presence").insert(UserPresenceInsert(user_id = userId))
            } catch (e: Exception) {
                Napier.w("user_presence creation failed", e, tag = TAG)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("Create user profile failed", e, tag = TAG)
            Result.failure(e)
        }
    }
    
    override suspend fun ensureProfileExists(userId: String, email: String): Result<Unit> {
        return try {
            val verifyProfile = client.from("users").select(columns = Columns.raw("uid")) {
                filter { eq("uid", userId) }
            }.decodeList<Map<String, String>>()
            
            if (verifyProfile.isNotEmpty()) return Result.success(Unit)
            
            Napier.d("Profile not found for user: $userId. Creating default profile.", tag = TAG)
            
            val username = email.substringBefore("@")
            createUserProfile(userId, email, username)
        } catch (e: Exception) {
            Napier.e("Failed to ensure profile exists for user: $userId", e, tag = TAG)
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUserId(): String? {
        return try {
            client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getCurrentUserEmail(): String? {
        return try {
            client.auth.currentUserOrNull()?.email
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun isEmailVerified(): Boolean {
        return try {
            client.auth.currentUserOrNull()?.emailConfirmedAt != null
        } catch (e: Exception) {
            false
        }
    }
    
    @OptIn(ExperimentalTime::class)
    override suspend fun restoreSession(): Boolean {
        return try {
            if (client.auth.currentSessionOrNull() != null) return true
            
            val storedTokens = tokenManager.getStoredTokens() ?: return false
            if (!tokenManager.areTokensValid()) {
                tokenManager.clearTokens()
                return false
            }
            
            val userSession = UserSession(
                accessToken = storedTokens.accessToken,
                refreshToken = storedTokens.refreshToken,
                expiresIn = (storedTokens.expiryTime - getCurrentTimeMillis()) / 1000,
                tokenType = "Bearer",
                user = null
            )
            
            client.auth.importSession(userSession)
            client.auth.currentUserOrNull() != null
        } catch (e: Exception) {
            Napier.e("Failed to restore session", e, tag = TAG)
            false
        }
    }
    
    @OptIn(ExperimentalTime::class)
    override suspend fun refreshSession(): Result<Unit> {
        return try {
            client.auth.refreshCurrentSession()
            storeCurrentSessionTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("Failed to refresh session", e, tag = TAG)
            Result.failure(e)
        }
    }
    
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            client.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("Failed to send password reset email", e, tag = TAG)
            Result.failure(e)
        }
    }
    
    override suspend fun resendVerificationEmail(email: String): Result<Unit> {
        return try {
            client.auth.resendEmail(OtpType.Email.SIGNUP, email)
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("Failed to resend verification email", e, tag = TAG)
            Result.failure(e)
        }
    }
    
    override suspend fun updatePassword(password: String): Result<Unit> {
        return try {
            client.auth.updateUser {
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("Failed to update password", e, tag = TAG)
            Result.failure(e)
        }
    }
    
    override suspend fun updateEmail(email: String): Result<Unit> {
        return try {
            client.auth.updateUser {
                this.email = email
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("Failed to update email", e, tag = TAG)
            Result.failure(e)
        }
    }
    
    override suspend fun getOAuthUrl(provider: String, redirectUrl: String): Result<String> {
        return try {
            val supabaseUrl = SupabaseClient.url
            val baseUrl = if (supabaseUrl.endsWith("/")) supabaseUrl.dropLast(1) else supabaseUrl
            
            val providerName = when(provider.lowercase()) {
                "google" -> "google"
                "apple" -> "apple"
                else -> provider.lowercase()
            }
            
            val url = "$baseUrl/auth/v1/authorize?provider=$providerName&redirect_to=$redirectUrl"
            Result.success(url)
        } catch (e: Exception) {
            Napier.e("Failed to get OAuth URL", e, tag = TAG)
            Result.failure(e)
        }
    }
    
    @OptIn(ExperimentalTime::class)
    override suspend fun handleOAuthCallback(code: String?, accessToken: String?, refreshToken: String?): Result<Unit> {
        return try {
            when {
                code != null -> {
                    // Authorization Code Flow
                    client.auth.exchangeCodeForSession(code)
                    storeCurrentSessionTokens()
                    
                    val currentUser = client.auth.currentUserOrNull()
                    if (currentUser?.email != null) {
                        ensureProfileExists(currentUser.id, currentUser.email!!)
                    }
                }
                accessToken != null && refreshToken != null -> {
                    // Implicit Flow
                    val userSession = UserSession(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        expiresIn = 3600,
                        tokenType = "Bearer",
                        user = null
                    )
                    client.auth.importSession(userSession)
                    storeCurrentSessionTokens()
                    
                    val currentUser = client.auth.currentUserOrNull()
                    if (currentUser?.email != null) {
                        ensureProfileExists(currentUser.id, currentUser.email!!)
                    }
                }
                else -> {
                    return Result.failure(Exception("No valid session tokens found in callback"))
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("Failed to handle OAuth callback", e, tag = TAG)
            Result.failure(e)
        }
    }
    
    override suspend fun storeSessionTokens(accessToken: String, refreshToken: String, userId: String, userEmail: String, expiresIn: Int) {
        tokenManager.storeTokens(accessToken, refreshToken, userId, userEmail, expiresIn)
    }
    
    override suspend fun clearStoredTokens() {
        tokenManager.clearTokens()
    }
    
    private suspend fun storeCurrentSessionTokens() {
        try {
            val currentSession = client.auth.currentSessionOrNull()
            val currentUser = client.auth.currentUserOrNull()
            
            if (currentSession != null && currentUser != null) {
                storeSessionTokens(
                    accessToken = currentSession.accessToken,
                    refreshToken = currentSession.refreshToken ?: "",
                    userId = currentUser.id,
                    userEmail = currentUser.email ?: "",
                    expiresIn = (currentSession.expiresIn ?: 3600).toInt()
                )
            }
        } catch (e: Exception) {
            Napier.w("Failed to store session tokens", e, tag = TAG)
        }
    }
    
    private fun getCurrentTimeMillis(): Long {
        return com.synapse.social.studioasinc.shared.core.util.getCurrentTimeMillis()
    }
}
