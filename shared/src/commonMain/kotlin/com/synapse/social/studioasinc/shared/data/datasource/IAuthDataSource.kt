package com.synapse.social.studioasinc.shared.data.datasource

import com.synapse.social.studioasinc.shared.domain.model.OAuthProvider

interface IAuthDataSource {
    suspend fun signUp(email: String, password: String): Result<String>
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun signInWithOAuth(provider: OAuthProvider, redirectUrl: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUserId(): String?
    suspend fun refreshSession(): Result<Unit>
}
