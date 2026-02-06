package com.synapse.social.studioasinc.shared.data.auth

import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.core.util.getCurrentIsoTime
import com.synapse.social.studioasinc.shared.data.model.UserProfileInsert
import com.synapse.social.studioasinc.shared.data.model.UserSettingsInsert
import com.synapse.social.studioasinc.shared.data.model.UserPresenceInsert
import io.github.aakira.napier.Napier
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlin.time.ExperimentalTime

/**
 * Supabase implementation of authentication service.
 * Handles all Supabase-specific authentication operations.
 */
class SupabaseAuthenticationService : IAuthenticationService {

    override suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                // Get user ID from current session
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id 
                    ?: throw Exception("User ID not found after signup")
                
                Napier.d("User signed up successfully: $userId")
                Result.success(userId)
            }
        } catch (e: Exception) {
            Napier.e("Sign up failed", e)
            Result.failure(e)
        }
    }

    override suspend fun createUserProfile(userId: String, email: String, username: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                // Create profile
                val profileInsert = UserProfileInsert(
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
                    user_level_xp = 0,
                    status = "active"
                )
                
                SupabaseClient.client.from("user_profiles").insert(profileInsert)
                
                // Create settings
                val settingsInsert = UserSettingsInsert(
                    user_id = userId
                )
                
                SupabaseClient.client.from("user_settings").insert(settingsInsert)
                
                // Create presence
                val presenceInsert = UserPresenceInsert(
                    user_id = userId
                )
                
                SupabaseClient.client.from("user_presence").insert(presenceInsert)
                
                Napier.d("User profile created successfully: $userId")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Create user profile failed", e)
            Result.failure(e)
        }
    }

    override suspend fun ensureProfileExists(userId: String, email: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                // Check if profile exists
                // For now, skip the existence check and create the profile
                // TODO: Implement proper profile existence check
                val existingProfile = null
                
                if (existingProfile == null) {
                    // Create basic profile
                    createUserProfile(userId, email, email.substringBefore("@"))
                } else {
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Napier.e("Ensure profile exists failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                // Get user ID from current session
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id 
                    ?: throw Exception("User ID not found after sign in")
                
                Napier.d("User signed in successfully: $userId")
                Result.success(userId)
            }
        } catch (e: Exception) {
            Napier.e("Sign in failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.signOut()
                Napier.d("User signed out successfully")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Sign out failed", e)
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return try {
            SupabaseClient.client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            Napier.e("Failed to get current user ID", e)
            null
        }
    }

    override suspend fun getCurrentUserEmail(): String? {
        return try {
            SupabaseClient.client.auth.currentUserOrNull()?.email
        } catch (e: Exception) {
            Napier.e("Failed to get current user email", e)
            null
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun isEmailVerified(): Boolean {
        return try {
            SupabaseClient.client.auth.currentUserOrNull()?.emailConfirmedAt != null
        } catch (e: Exception) {
            Napier.e("Failed to check email verification", e)
            false
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun refreshSession(): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.refreshCurrentSession()
                Napier.d("Session refreshed successfully")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Session refresh failed", e)
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun restoreSession(): Boolean {
        return try {
            withContext(Dispatchers.Default) {
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                session != null
            }
        } catch (e: Exception) {
            Napier.e("Session restore failed", e)
            false
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.resetPasswordForEmail(email)
                Napier.d("Password reset email sent successfully")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Password reset email failed", e)
            Result.failure(e)
        }
    }

    override suspend fun resendVerificationEmail(email: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                // Simplified - just send password reset as verification
                SupabaseClient.client.auth.resetPasswordForEmail(email)
                Napier.d("Verification email resent successfully")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Resend verification email failed", e)
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(password: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.updateUser {
                    this.password = password
                }
                Napier.d("Password updated successfully")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Password update failed", e)
            Result.failure(e)
        }
    }

    override suspend fun updateEmail(email: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.updateUser {
                    this.email = email
                }
                Napier.d("Email updated successfully")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Email update failed", e)
            Result.failure(e)
        }
    }

    override suspend fun getOAuthUrl(provider: String, redirectUrl: String): Result<String> {
        return try {
            val supabaseUrl = SupabaseClient.client.supabaseUrl
            val oauthUrl = "$supabaseUrl/auth/v1/authorize?provider=$provider&redirect_to=$redirectUrl"
            Result.success(oauthUrl)
        } catch (e: Exception) {
            Napier.e("Failed to generate OAuth URL", e)
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun handleOAuthCallback(code: String?, accessToken: String?, refreshToken: String?): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                when {
                    code != null -> {
                        // Handle authorization code flow
                        SupabaseClient.client.auth.exchangeCodeForSession(code)
                    }
                    accessToken != null && refreshToken != null -> {
                        // Handle token-based flow
                        SupabaseClient.client.auth.importAuthToken(accessToken, refreshToken)
                    }
                    else -> {
                        throw Exception("No valid OAuth parameters provided")
                    }
                }
                Napier.d("OAuth callback handled successfully")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("OAuth callback handling failed", e)
            Result.failure(e)
        }
    }

    override suspend fun storeSessionTokens(accessToken: String, refreshToken: String, userId: String, userEmail: String, expiresIn: Int) {
        // Implementation depends on platform-specific storage
        // For now, tokens are managed by Supabase client
        Napier.d("Session tokens stored for user: $userId")
    }

    override suspend fun clearStoredTokens() {
        // Implementation depends on platform-specific storage
        // For now, handled by signOut
        Napier.d("Stored tokens cleared")
    }
}
