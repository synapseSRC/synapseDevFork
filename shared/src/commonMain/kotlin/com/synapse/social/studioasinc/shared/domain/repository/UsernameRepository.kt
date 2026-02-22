package com.synapse.social.studioasinc.shared.domain.repository

interface UsernameRepository {
    suspend fun isUsernameAvailable(username: String): Result<Boolean>
    suspend fun updateUsername(userId: String, username: String): Result<Unit>
}
