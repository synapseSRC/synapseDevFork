package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.User
import com.synapse.social.studioasinc.shared.domain.model.UserProfile

interface UserRepository {
    suspend fun getUserById(userId: String): Result<User?>
    suspend fun getUserByUsername(username: String): Result<UserProfile?>
    suspend fun updateUser(user: UserProfile): Result<UserProfile>
    suspend fun searchUsers(query: String, limit: Int = 20): Result<List<UserProfile>>
    suspend fun checkUsernameAvailability(username: String): Result<Boolean>
    suspend fun isUsernameAvailable(username: String): Result<Boolean>
}
