package com.synapse.social.studioasinc.shared.data.auth

import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.core.util.getCurrentIsoTime
import com.synapse.social.studioasinc.shared.data.model.UserProfileInsert
import com.synapse.social.studioasinc.shared.data.model.UserSettingsInsert
import com.synapse.social.studioasinc.shared.data.model.UserPresenceInsert
import io.github.aakira.napier.Napier
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
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
            withContext(Dispatchers.IO) {
                val result = SupabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                val userId = result.user?.id ?: throw Exception("User ID not found after signup")
                Napier.d("User signed up successfully: $userId")
                Result.success(userId)
            }
        } catch (e: Exception) {
            Napier.e("Sign up failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signUpWithProfile(email: String, password: String, username: String): Result<String> {
        return try {
            withContext(Dispatchers.IO) {
                // First sign up the user
                val signUpResult = SupabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                val userId = signUpResult.user?.id ?: throw Exception("User ID not found after signup")
                
                // Create profile
                val profileInsert = UserProfileInsert(
                    id = userId,
                    username = username,
                    email = email,
                    displayName = username,
                    bio = null,
                    avatarUrl = null,
                    bannerUrl = null,
                    location = null,
                    website = null,
                    isVerified = false,
                    isPrivate = false,
                    followersCount = 0,
                    followingCount = 0,
                    postsCount = 0,
                    createdAt = getCurrentIsoTime(),
                    updatedAt = getCurrentIsoTime()
                )
                
                SupabaseClient.from("user_profiles").insert(profileInsert)
                
                // Create settings
                val settingsInsert = UserSettingsInsert(
                    userId = userId,
                    theme = "system",
                    language = "en",
                    notificationsEnabled = true,
                    emailNotifications = true,
                    pushNotifications = true,
                    privacyLevel = "public",
                    showOnlineStatus = true,
                    allowDirectMessages = true,
                    allowTagging = true,
                    contentFilter = "moderate",
                    autoplayVideos = true,
                    reduceMotion = false,
                    highContrast = false,
                    createdAt = getCurrentIsoTime(),
                    updatedAt = getCurrentIsoTime()
                )
                
                SupabaseClient.from("user_settings").insert(settingsInsert)
                
                // Create presence
                val presenceInsert = UserPresenceInsert(
                    userId = userId,
                    status = "offline",
                    lastSeen = getCurrentIsoTime(),
                    isOnline = false,
                    currentActivity = null,
                    updatedAt = getCurrentIsoTime()
                )
                
                SupabaseClient.from("user_presence").insert(presenceInsert)
                
                Napier.d("User signed up with profile successfully: $userId")
                Result.success(userId)
            }
        } catch (e: Exception) {
            Napier.e("Sign up with profile failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            withContext(Dispatchers.IO) {
                val result = SupabaseClient.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                val userId = result.user?.id ?: throw Exception("User ID not found after sign in")
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
            withContext(Dispatchers.IO) {
                SupabaseClient.auth.signOut()
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
            SupabaseClient.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            Napier.e("Failed to get current user ID", e)
            null
        }
    }

    override suspend fun getCurrentUserEmail(): String? {
        return try {
            SupabaseClient.auth.currentUserOrNull()?.email
        } catch (e: Exception) {
            Napier.e("Failed to get current user email", e)
            null
        }
    }

    override suspend fun isEmailVerified(): Boolean {
        return try {
            SupabaseClient.auth.currentUserOrNull()?.emailConfirmedAt != null
        } catch (e: Exception) {
            Napier.e("Failed to check email verification", e)
            false
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun refreshSession(): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                SupabaseClient.auth.refreshCurrentSession()
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
            withContext(Dispatchers.IO) {
                val session = SupabaseClient.auth.currentSessionOrNull()
                session != null
            }
        } catch (e: Exception) {
            Napier.e("Session restore failed", e)
            false
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                SupabaseClient.auth.resetPasswordForEmail(email)
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
            withContext(Dispatchers.IO) {
                SupabaseClient.auth.resend(email = email, type = io.github.jan.supabase.auth.providers.builtin.OTP.EMAIL)
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
            withContext(Dispatchers.IO) {
                SupabaseClient.auth.updateUser {
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

    override suspend fun getOAuthUrl(provider: String, redirectUrl: String): Result<String> {
        return try {
            val supabaseUrl = SupabaseClient.supabaseUrl
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
            withContext(Dispatchers.IO) {
                when {
                    code != null -> {
                        // Handle authorization code flow
                        SupabaseClient.auth.exchangeCodeForSession(code)
                    }
                    accessToken != null && refreshToken != null -> {
                        // Handle token-based flow
                        SupabaseClient.auth.importAuthToken(accessToken, refreshToken)
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
}
