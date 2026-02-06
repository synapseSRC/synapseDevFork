package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.OAuthProvider
import io.github.jan.supabase.auth.OtpType
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import io.github.aakira.napier.Napier
import kotlin.time.ExperimentalTime

/**
 * Shared Authentication Repository
 * Simplified version using direct Supabase calls
 */
class AuthRepository {
    private val TAG = "AuthRepository"

    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id 
                    ?: throw Exception("User ID not found")
                Napier.d("User signed up: ", tag = TAG)
                Result.success(userId)
            }
        } catch (e: Exception) {
            Napier.e("Sign up failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun signUpWithProfile(email: String, password: String, username: String): Result<String> {
        return signUp(email, password) // Simplified for now
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id 
                    ?: throw Exception("User ID not found")
                Napier.d("User signed in: ", tag = TAG)
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
                SupabaseClient.client.auth.signOut()
                Napier.d("User signed out", tag = TAG)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Sign out failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserId(): String? {
        return try {
            SupabaseClient.client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            Napier.e("Failed to get current user ID", e, tag = TAG)
            null
        }
    }

    suspend fun getCurrentUserEmail(): String? {
        return try {
            SupabaseClient.client.auth.currentUserOrNull()?.email
        } catch (e: Exception) {
            Napier.e("Failed to get current user email", e, tag = TAG)
            null
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun isEmailVerified(): Boolean {
        return try {
            SupabaseClient.client.auth.currentUserOrNull()?.emailConfirmedAt != null
        } catch (e: Exception) {
            Napier.e("Failed to check email verification", e, tag = TAG)
            false
        }
    }

    suspend fun refreshSession(): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.refreshCurrentSession()
                Napier.d("Session refreshed", tag = TAG)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Session refresh failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun restoreSession(): Boolean {
        return try {
            SupabaseClient.client.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            Napier.e("Session restore failed", e, tag = TAG)
            false
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.resetPasswordForEmail(email)
                Napier.d("Password reset email sent", tag = TAG)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Password reset failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun resendVerificationEmail(email: String): Result<Unit> {
        return sendPasswordResetEmail(email) // Simplified
    }

    suspend fun updatePassword(password: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.updateUser {
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
            val supabaseUrl = SupabaseClient.client.supabaseUrl
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
                        SupabaseClient.client.auth.exchangeCodeForSession(code)
                    }
                    accessToken != null && refreshToken != null -> {
                        SupabaseClient.client.auth.importAuthToken(accessToken, refreshToken)
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
                SupabaseClient.client.auth.signInWith(provider)
                Napier.d("OAuth sign-in initiated for ${provider.name}", tag = TAG)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("OAuth sign-in failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun updatePhoneNumber(phone: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.updateUser {
                    this.phone = phone
                }
                Napier.d("Phone number update initiated: $phone", tag = TAG)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Phone number update failed", e, tag = TAG)
            Result.failure(e)
        }
    }

    suspend fun verifyPhoneChange(phone: String, token: String): Result<Unit> {
        return try {
            withContext(Dispatchers.Default) {
                SupabaseClient.client.auth.verifyPhoneOtp(
                    type = OtpType.Phone.SMS,
                    token = token,
                    phone = phone
                )
                Napier.d("Phone number verified and updated", tag = TAG)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Napier.e("Phone verification failed", e, tag = TAG)
            Result.failure(e)
        }
    }
}
