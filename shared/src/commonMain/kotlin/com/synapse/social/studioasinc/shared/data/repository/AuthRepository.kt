package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.data.auth.TokenManager
import com.synapse.social.studioasinc.shared.data.model.UserProfileInsert
import com.synapse.social.studioasinc.shared.data.model.UserSettingsInsert
import com.synapse.social.studioasinc.shared.data.model.UserPresenceInsert
import com.synapse.social.studioasinc.shared.core.util.getCurrentIsoTime
import com.synapse.social.studioasinc.shared.core.util.getCurrentTimeMillis
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay

class AuthRepository(
    private val tokenManager: TokenManager
) {
    private val client = SupabaseClient.client
    private val TAG = "AuthRepository"

    suspend fun signUp(email: String, password: String): Result<String> {
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

    suspend fun signUpWithProfile(email: String, password: String, username: String): Result<String> {
        return try {
            // 1. Create Auth User
            val user = client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val authUserId = client.auth.currentUserOrNull()?.id ?: user?.id
                ?: throw Exception("Failed to get user ID")

            // 2. Create Profile
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

            // 3. Create Settings/Presence (Best Effort)
            try {
                client.from("user_settings").insert(UserSettingsInsert(user_id = authUserId))
            } catch (e: Exception) {
                Napier.w("user_settings creation failed", e, tag = TAG)
            }

            try {
                client.from("user_presence").insert(UserPresenceInsert(user_id = authUserId))
            } catch (e: Exception) {
                Napier.w("user_presence creation failed", e, tag = TAG)
            }

            // 4. Verify Profile Creation
            delay(100) // Consistency delay
            val verifyProfile = client.from("users").select(columns = Columns.raw("uid")) {
                filter { eq("uid", authUserId) }
            }.decodeList<Map<String, String>>()

            if (verifyProfile.isEmpty()) {
                Napier.e("Profile verification failed for user: $authUserId", tag = TAG)
                // Proceed anyway as insert didn't throw
            }

            storeCurrentSessionTokens()
            Result.success(authUserId)
        } catch (e: Exception) {
            Napier.e("Sign up with profile failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<String> {
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

    suspend fun signOut(): Result<Unit> {
        return try {
            client.auth.signOut()
            tokenManager.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.clearTokens() // Clear anyway
            Napier.e("Sign out failed", e, tag = TAG)
            Result.failure(e)
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
            Napier.w("Failed to store session tokens", e, tag = TAG)
        }
    }

    private suspend fun ensureProfileExists(userId: String, email: String) {
        try {
            val verifyProfile = client.from("users").select(columns = Columns.raw("uid")) {
                filter { eq("uid", userId) }
            }.decodeList<Map<String, String>>()

            if (verifyProfile.isNotEmpty()) return

            Napier.d("Profile not found for user: $userId. Creating default profile.", tag = TAG)

            val username = email.substringBefore("@")
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
        } catch (e: Exception) {
            Napier.e("Failed to ensure profile exists for user: $userId", e, tag = TAG)
            // We log but maybe we shouldn't fail signIn entirely?
            // The prompt says "Logic to ensure the user profile exists... similar to what is done in 'signUpWithProfile'".
            // In signUpWithProfile, exception in main block fails the result.
            // So if profile creation fails here, we should probably throw (which will return Result.failure in signIn).
            throw e
        }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    suspend fun restoreSession(): Boolean {
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
}
