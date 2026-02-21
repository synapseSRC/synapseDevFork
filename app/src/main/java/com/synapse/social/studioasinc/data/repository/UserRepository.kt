package com.synapse.social.studioasinc.data.repository

import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.domain.model.User
import com.synapse.social.studioasinc.domain.model.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.synapse.social.studioasinc.core.network.SupabaseErrorHandler
import com.synapse.social.studioasinc.core.network.SupabaseClient as AppSupabaseClient
import com.synapse.social.studioasinc.shared.core.network.SupabaseClient as SharedSupabaseClient
import com.synapse.social.studioasinc.shared.domain.repository.UserRepository as SharedUserRepository

@Singleton
class UserRepository constructor(
    private val storageDatabase: StorageDatabase,
    private val client: SupabaseClient = AppSupabaseClient.client
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
            return SupabaseErrorHandler.toResult(e, "UserRepository", "Failed to fetch user by ID: $userId")
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
            return SupabaseErrorHandler.toResult(e, "UserRepository", "Failed to fetch user by username: $username")
        }
    }

    suspend fun updateUser(user: UserProfile): Result<UserProfile> {
        return try {
            if (user.uid.isBlank()) {
                return Result.failure(Exception("User ID cannot be empty"))
            }

            val updateData = mapOf(
                "username" to user.username,
                "display_name" to user.displayName,
                "email" to user.email,
                "bio" to user.bio,
                "avatar" to user.avatar,
                "followers_count" to user.followersCount,
                "following_count" to user.followingCount,
                "posts_count" to user.postsCount,
                "status" to user.status,
                "account_type" to user.account_type,
                "verify" to user.verify,
                "banned" to user.banned
            )

            client.from(SharedSupabaseClient.TABLE_USERS)
                .update(updateData) {
                    filter {
                        eq("uid", user.uid)
                    }
                }

            android.util.Log.d("UserRepository", "User updated successfully: ${user.uid}")
            Result.success(user)
        } catch (e: Exception) {
            return SupabaseErrorHandler.toResult(e, "UserRepository", "Failed to update user: ${user.uid}")
        }
    }

    suspend fun searchUsers(query: String, limit: Int = 20): Result<List<UserProfile>> {
        return try {
            if (query.isBlank()) {
                return Result.success(emptyList())
            }

            val users = client.from(SharedSupabaseClient.TABLE_USERS)
                .select() {
                    filter {
                        or {
                            ilike("username", "%$query%")
                            ilike("display_name", "%$query%")
                        }
                    }
                    limit(limit.toLong())
                }
                .decodeList<UserProfile>()

            android.util.Log.d("UserRepository", "Search found ${users.size} users for query: $query")
            Result.success(users)
        } catch (e: Exception) {
            return SupabaseErrorHandler.toResult(e, "UserRepository", "Failed to search users with query: $query")
        }
    }

    suspend fun checkUsernameAvailability(username: String): Result<Boolean> {
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
            return SupabaseErrorHandler.toResult(e, "UserRepository", "Failed to check username availability: $username")
        }
    }

    override suspend fun isUsernameAvailable(username: String): Result<Boolean> {
        return checkUsernameAvailability(username)
    }
}
