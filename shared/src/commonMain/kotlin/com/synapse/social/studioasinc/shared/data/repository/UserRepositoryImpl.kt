package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.core.network.SupabaseClient as SharedSupabaseClient
import com.synapse.social.studioasinc.shared.core.network.SupabaseErrorHandler
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.domain.model.User
import com.synapse.social.studioasinc.shared.domain.model.UserProfile
import com.synapse.social.studioasinc.shared.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.booleanOrNull

class UserRepositoryImpl(
    private val storageDatabase: StorageDatabase,
    private val client: SupabaseClient = SharedSupabaseClient.client
) : UserRepository {

    override suspend fun getUserById(userId: String): Result<User?> {
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

    override suspend fun getUserByUsername(username: String): Result<UserProfile?> {
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

    override suspend fun updateUser(user: UserProfile): Result<UserProfile> {
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

            Napier.d("User updated successfully: ${user.uid}", tag = "UserRepository")
            Result.success(user)
        } catch (e: Exception) {
            return SupabaseErrorHandler.toResult(e, "UserRepository", "Failed to update user: ${user.uid}")
        }
    }

    override suspend fun searchUsers(query: String, limit: Int): Result<List<UserProfile>> {
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

            Napier.d("Search found ${users.size} users for query: $query", tag = "UserRepository")
            Result.success(users)
        } catch (e: Exception) {
            return SupabaseErrorHandler.toResult(e, "UserRepository", "Failed to search users with query: $query")
        }
    }

    override suspend fun checkUsernameAvailability(username: String): Result<Boolean> {
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
