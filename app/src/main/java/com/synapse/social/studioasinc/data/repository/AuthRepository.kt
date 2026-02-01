package com.synapse.social.studioasinc.data.repository

import android.net.Uri
import com.synapse.social.studioasinc.core.network.SupabaseClient
import com.synapse.social.studioasinc.data.local.auth.TokenManager
import com.synapse.social.studioasinc.shared.data.repository.AuthRepository as SharedAuthRepository
import com.synapse.social.studioasinc.shared.data.auth.TokenManager as SharedTokenManager
import com.synapse.social.studioasinc.shared.data.model.UserProfileInsert
import com.synapse.social.studioasinc.shared.data.model.UserSettingsInsert
import com.synapse.social.studioasinc.shared.data.model.UserPresenceInsert
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling authentication operations with Supabase.
 *
 * ARCHITECTURAL NOTE:
 * This class now acts as a facade over the Shared Module's AuthRepository.
 * Most business logic has been moved to the Shared Module to ensure consistency
 * across platforms (Android, iOS, Web).
 *
 * Platform-specific logic (like OAuth deep linking) remains here.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val tokenManager: TokenManager, // Legacy TokenManager (keep for migration/compat)
    private val sharedAuthRepository: SharedAuthRepository // Shared Logic Delegate
) {

    // Facade for accessing shared Supabase client
    private val client = SupabaseClient.client

    private fun isSupabaseConfigured(): Boolean = SupabaseClient.isConfigured()

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
        // Clear legacy tokens first to avoid conflicts
        tokenManager.clearTokens()
        return sharedAuthRepository.signIn(email, password)
    }

    /**
     * Sign out. Delegates to Shared Module.
     */
    suspend fun signOut(): Result<Unit> {
        // Clear legacy tokens
        tokenManager.clearTokens()
        return sharedAuthRepository.signOut()
    }

    /**
     * Get the OAuth URL for a specific provider.
     * Platform-specific implementation (Android Deep Link).
     */
    suspend fun getOAuthUrl(provider: String): Result<String> {
        return try {
            if (!isSupabaseConfigured()) {
                return Result.failure(Exception("Supabase not configured"))
            }

            // We use the https URL which is registered in AndroidManifest.xml as autoVerify=true
            val redirectUrl = "https://synapseofficial.vercel.app/"

            // Use the client's URL and append the path
            val supabaseUrl = SupabaseClient.getUrl()
            // Remove trailing slash if present
            val baseUrl = if (supabaseUrl.endsWith("/")) supabaseUrl.dropLast(1) else supabaseUrl

            val providerName = when(provider.lowercase()) {
                "google" -> "google"
                "apple" -> "apple"
                else -> provider.lowercase()
            }

            val url = "$baseUrl/auth/v1/authorize?provider=$providerName&redirect_to=$redirectUrl"
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Handle OAuth callback from deep link.
     * Platform-specific implementation.
     */
    suspend fun handleOAuthCallback(uri: Uri): Result<Unit> {
        return try {
            if (!isSupabaseConfigured()) {
                return Result.failure(Exception("Supabase not configured"))
            }

            // Check for error
            val error = uri.getQueryParameter("error_description") ?: uri.getQueryParameter("error")
            if (error != null) {
                return Result.failure(Exception(error))
            }

            // 1. Check for PKCE 'code' (Authorization Code Flow)
            val code = uri.getQueryParameter("code")
            if (code != null) {
                 try {
                     client.auth.exchangeCodeForSession(code)

                     // Store tokens and ensure profile exists
                     storeCurrentSessionTokens()
                     val currentUser = client.auth.currentUserOrNull()
                     if (currentUser != null && currentUser.email != null) {
                         val profileExists = ensureProfileExistsWithVerification(currentUser.id, currentUser.email!!)
                         if (!profileExists) {
                             throw Exception("Failed to create or verify user profile after OAuth login")
                         }
                     }

                     return Result.success(Unit)
                 } catch (e: Exception) {
                     android.util.Log.e("AuthRepository", "Failed to exchange code", e)
                 }
            }

            // 2. Check for Implicit Flow (tokens in fragment)
            val fragment = uri.fragment
            if (fragment != null) {
                 val params = fragment.split("&").associate {
                     val parts = it.split("=")
                     if (parts.size == 2) parts[0] to parts[1] else "" to ""
                 }

                 val accessToken = params["access_token"]
                 val refreshToken = params["refresh_token"]

                 if (accessToken != null && refreshToken != null) {
                     val userSession = UserSession(
                         accessToken = accessToken,
                         refreshToken = refreshToken,
                         expiresIn = 3600,
                         tokenType = "Bearer",
                         user = null
                     )
                     client.auth.importSession(userSession)

                     // Store tokens and ensure profile exists
                     storeCurrentSessionTokens()
                     val currentUser = client.auth.currentUserOrNull()
                     if (currentUser != null && currentUser.email != null) {
                         val profileExists = ensureProfileExistsWithVerification(currentUser.id, currentUser.email!!)
                         if (!profileExists) {
                             throw Exception("Failed to create or verify user profile after OAuth login")
                         }
                     }

                     return Result.success(Unit)
                 }
            }

            Result.failure(Exception("No valid session tokens found in callback"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send a password reset email. Direct Client call (could be moved to Shared).
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            if (!isSupabaseConfigured()) {
                return Result.failure(Exception("Supabase not configured"))
            }
            client.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resend verification email. Direct Client call.
     */
    suspend fun resendVerificationEmail(email: String): Result<Unit> {
        return try {
            if (!isSupabaseConfigured()) {
                return Result.failure(Exception("Supabase not configured"))
            }
            client.auth.resendEmail(OtpType.Email.SIGNUP, email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user password. Direct Client call.
     */
    suspend fun updateUserPassword(password: String): Result<Unit> {
        return try {
            if (!isSupabaseConfigured()) {
                return Result.failure(Exception("Supabase not configured"))
            }
            client.auth.updateUser {
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Recover session from access token.
     */
    suspend fun recoverSession(accessToken: String): Result<Unit> {
        // This is tricky without a refresh token, but we assume we are just trying to get into the app to reset password.
        // Shared module doesn't handle this yet, so keeping it here.
        return Result.success(Unit)
    }

    /**
     * Refresh the current session.
     */
    suspend fun refreshSession(): Result<Unit> {
        return try {
            if (!isSupabaseConfigured()) {
                return Result.failure(Exception("Supabase not configured"))
            }
            client.auth.refreshCurrentSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if current user's email is verified.
     */
    fun isEmailVerified(): Boolean {
        return if (isSupabaseConfigured()) {
            client.auth.currentUserOrNull()?.emailConfirmedAt != null
        } else {
            false
        }
    }

    /**
     * Get the current authenticated user's ID.
     */
    fun getCurrentUserId(): String? {
        return if (isSupabaseConfigured()) {
            try {
                client.auth.currentUserOrNull()?.id
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    /**
     * Get current user UID (maps to Auth ID).
     */
    suspend fun getCurrentUserUid(): String? {
        return getCurrentUserId() // Simplified as they are 1:1 in this architecture
    }

    fun getCurrentUserEmail(): String? {
        return if (isSupabaseConfigured()) {
            try {
                client.auth.currentUserOrNull()?.email
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun isUserLoggedIn(): Boolean {
        return if (isSupabaseConfigured()) {
            try {
                client.auth.currentUserOrNull() != null
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    fun observeAuthState(): Flow<Boolean> {
        return if (isSupabaseConfigured()) {
            try {
                client.auth.sessionStatus.map {
                    client.auth.currentUserOrNull() != null
                }
            } catch (e: Exception) {
                flowOf(false)
            }
        } else {
            flowOf(false)
        }
    }

    /**
     * Ensure profile exists public alias.
     */
    suspend fun ensureProfileExistsPublic(userId: String, email: String) {
       // Delegated logic usually happens in Shared, but if called explicitly,
       // we might need to expose a method in Shared or just trust the flow.
       // SharedAuthRepository.ensureProfileExists is private.
       // However, signIn automatically calls it.
    }


    /**
     * Restore user session from storage.
     * Manually implemented here to bridge Shared storage -> Android Lifecycle if needed,
     * OR we can add restoreSession to SharedAuthRepository.
     *
     * For now, we will implement it using the Shared components we have access to via SupabaseClient.
     */
    suspend fun restoreSession(): Boolean {
        return if (isSupabaseConfigured()) {
            sharedAuthRepository.restoreSession()
        } else {
            false
        }
    }

    private suspend fun storeCurrentSessionTokens() {
        try {
            val currentSession = client.auth.currentSessionOrNull()
            val currentUser = client.auth.currentUserOrNull()

            if (currentSession != null && currentUser != null) {
                tokenManager.storeTokens(
                    accessToken = currentSession.accessToken,
                    refreshToken = currentSession.refreshToken ?: "",
                    userId = currentUser.id,
                    userEmail = currentUser.email ?: "",
                    expiresIn = (currentSession.expiresIn ?: 3600).toInt()
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to store session tokens", e)
        }
    }

    private suspend fun ensureProfileExistsWithVerification(userId: String, email: String): Boolean {
        return try {
            val verifyProfile = client.from("users").select(columns = Columns.raw("uid")) {
                filter { eq("uid", userId) }
            }.decodeList<Map<String, String>>()

            if (verifyProfile.isNotEmpty()) return true

            android.util.Log.d("AuthRepository", "Profile not found for user: $userId. Creating default profile.")

            val username = email.substringBefore("@")
            val now = java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())

            val userProfile = UserProfileInsert(
                username = username,
                email = email,
                created_at = now,
                updated_at = now,
                join_date = now,
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

            try {
                client.from("user_settings").insert(UserSettingsInsert(user_id = userId))
            } catch (e: Exception) {
                android.util.Log.w("AuthRepository", "user_settings creation failed", e)
            }

            try {
                client.from("user_presence").insert(UserPresenceInsert(user_id = userId))
            } catch (e: Exception) {
                android.util.Log.w("AuthRepository", "user_presence creation failed", e)
            }

            true
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to ensure profile exists for user: $userId", e)
            false
        }
    }
}
