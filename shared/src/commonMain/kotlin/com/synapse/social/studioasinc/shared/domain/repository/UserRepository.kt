package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.User
import com.synapse.social.studioasinc.shared.domain.model.UserProfile

interface UserRepository {
    suspend fun isUsernameAvailable(username: String): Result<Boolean>
    suspend fun getUserById(userId: String): Result<User?>
}
