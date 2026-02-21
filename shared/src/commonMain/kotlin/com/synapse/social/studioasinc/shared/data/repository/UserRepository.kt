package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.domain.model.User
import com.synapse.social.studioasinc.shared.domain.model.UserProfile
import com.synapse.social.studioasinc.shared.data.mapper.UserMapper
import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import com.synapse.social.studioasinc.shared.core.network.SupabaseErrorHandler
import com.synapse.social.studioasinc.shared.core.network.SupabaseClient as SharedSupabaseClient
import com.synapse.social.studioasinc.shared.domain.repository.UserRepository as SharedUserRepository

class UserRepository(
    private val storageDatabase: StorageDatabase,
    private val client: SupabaseClient
) : SharedUserRepository {

    suspend fun getUserById(userId: String): Result<User?> {
        return try {
            var user = storageDatabase.userQueries.selectById(userId).executeAsOneOrNull()?.let { UserMapper.toModel(it) }
            if (user == null) {
                val userProfile = client.from(SharedSupabaseClient.TABLE_USERS)
                    .select() {
                        filter {
                            eq("uid", userId)
                        }
                    }
                    .decodeSingleOrNull<UserProfile>()

                userProfile?.let {
                    user = User(
                        uid = it.uid,
                        username = it.username,
                        email = it.email,
                        avatar = it.avatar,
                        verify = it.verify
                    )
                    storageDatabase.userQueries.insertUser(UserMapper.toEntity(user!!))
                }
            }
            Result.success(user)
        } catch (e: Exception) {
            return SupabaseErrorHandler.toResult(e, "UserRepository", "Failed to fetch user by ID: ")
        }
    }

    suspend fun getUserByUsername(username: String): Result<UserProfile?> {
        return try {
            if (username.isBlank()) {
                return Result.failure(Exception("Username cannot be empty"))
            }

            val user = client.from(SharedSupabaseClient.TABLE_USERS)
                .select() {
                    filter {
                        eq("username", username)
                    }
                }
                .decodeSingleOrNull<UserProfile>()

            Result.success(user)
        } catch (e: Exception) {
            return SupabaseErrorHandler.toResult(e, "UserRepository", "Failed to fetch user by username: ")
        }
    }

    override suspend fun isUsernameAvailable(username: String): Result<Boolean> {
        return try {
            val existingUser = client.from(SharedSupabaseClient.TABLE_USERS)
                .select() {
                    filter {
                        eq("username", username)
                    }
                }
                .decodeSingleOrNull<UserProfile>()

            Result.success(existingUser == null)
        } catch (e: Exception) {
            return SupabaseErrorHandler.toResult(e, "UserRepository", "Failed to check username availability: ")
        }
    }
}
