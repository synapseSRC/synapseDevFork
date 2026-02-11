package com.synapse.social.studioasinc.shared.domain.repository

interface UserRepository {
    suspend fun isUsernameAvailable(username: String): Result<Boolean>
}
