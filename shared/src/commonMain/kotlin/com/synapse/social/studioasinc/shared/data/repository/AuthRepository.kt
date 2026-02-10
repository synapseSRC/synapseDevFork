package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.data.model.UserProfileInsert
import com.synapse.social.studioasinc.shared.data.model.UserSettingsInsert
import com.synapse.social.studioasinc.shared.data.model.UserPresenceInsert
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.OAuthProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import io.github.aakira.napier.Napier
import kotlin.time.ExperimentalTime
import io.github.jan.supabase.SupabaseClient as SupabaseClientLib


class AuthRepository(private val client: SupabaseClientLib = SupabaseClient.client) {
    private val TAG = "AuthRepository"

    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            withContext(Dispatchers.Default) {
                client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                val userId = client.auth.currentUserOrNull()?.id
                    ?: throw Exception("User ID not found")
                Napier.d("User signed up: $userId", tag = TAG)
                Result.success(userId)
            }
        } catch (e: Exception) {
            Napier.e("Sign up failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun signUpWithProfile(email: String, password: String, username: String): Result<String> {
        return try {
            val signUpResult = signUp(email, password)
            if (signUpResult.isSuccess) {
                val userId = signUpResult.getOrThrow()
                ensureProfileExists(userId, email, username).map { userId }
            } else {
                signUpResult
            }
        } catch (e: Exception) {
            Napier.e("Sign up with profile failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun ensureProfileExists(userId: String, email: String, username: String? = null): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {

                val count = client.from("user_profiles").select(columns = Columns.list("id")) {
                    count(Count.EXACT)
                    filter {
                        eq("id", userId)
                    }
                }.countOrNull()

                if (count == null || count == 0L) {
                    val actualUsername = username ?: email.substringBefore("@")


                    val profileInsert = UserProfileInsert(
                        username = actualUsername,
                        email = email
                    )
                    client.from("user_profiles").insert(profileInsert)


                    val settingsInsert = UserSettingsInsert(user_id = userId)
                    client.from("user_settings").insert(settingsInsert)


                    val presenceInsert = UserPresenceInsert(user_id = userId)
                    client.from("user_presence").insert(presenceInsert)

                    Napier.d("User profile created: $userId", tag = TAG)
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Ensure profile exists failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            withContext(Dispatchers.Default) {
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val userId = client.auth.currentUserOrNull()?.id
                    ?: throw Exception("User ID not found")
                Napier.d("User signed in: $userId", tag = TAG)
                Result.success(userId)
            }
        } catch (e: Exception) {
            Napier.e("Sign in failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                client.auth.signOut()
                Napier.d("User signed out", tag = TAG)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Sign out failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? {
        return try {
            client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            Napier.e("Failed to get current user ID", e, tag = TAG)
            null
        }
    }

    fun getCurrentUserEmail(): String? {
        return try {
            client.auth.currentUserOrNull()?.email
        } catch (e: Exception) {
            Napier.e("Failed to get current user email", e, tag = TAG)
            null
        }
    }

    @OptIn(ExperimentalTime::class)
    fun isEmailVerified(): Boolean {
        return try {
            client.auth.currentUserOrNull()?.emailConfirmedAt != null
        } catch (e: Exception) {
            Napier.e("Failed to check email verification", e, tag = TAG)
            false
        }
    }

    suspend fun refreshSession(): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                client.auth.refreshCurrentSession()
                Napier.d("Session refreshed", tag = TAG)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Session refresh failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    fun restoreSession(): Boolean {
        return try {
            client.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            Napier.e("Session restore failed", e, tag = TAG)
            false
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                client.auth.resetPasswordForEmail(email)
                Napier.d("Password reset email sent", tag = TAG)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Password reset failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun resendVerificationEmail(email: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                client.auth.resendEmail(OtpType.Email.SIGNUP, email)
                Napier.d("Verification email resent", tag = TAG)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Resend verification email failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun updatePassword(password: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                client.auth.updateUser {
                    this.password = password
                }
                Napier.d("Password updated", tag = TAG)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Password update failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun getOAuthUrl(provider: String, redirectUrl: String): Result<String> {
        return try {
            val supabaseUrl = client.supabaseUrl
            val oauthUrl = "$supabaseUrl/auth/v1/authorize?provider=$provider&redirect_to=$redirectUrl"
            Result.success(oauthUrl)
        } catch (e: Exception) {
            Napier.e("OAuth URL generation failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun handleOAuthCallback(code: String?, accessToken: String?, refreshToken: String?): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                when {
                    code != null -> {
                        client.auth.exchangeCodeForSession(code)
                    }
                    accessToken != null && refreshToken != null -> {
                        client.auth.importAuthToken(accessToken, refreshToken)
                    }
                    else -> {
                        throw Exception("No valid OAuth parameters")
                    }
                }
                Napier.d("OAuth callback handled", tag = TAG)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("OAuth callback failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun signInWithOAuth(provider: OAuthProvider, redirectUrl: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                client.auth.signInWith(provider)
                Napier.d("OAuth sign-in initiated for ${provider.name}", tag = TAG)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("OAuth sign-in failed", e, tag = TAG)
            Result.failure(e)
        }
    }
}
