package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.core.config.SynapseConfig
import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.domain.model.User
import com.synapse.social.studioasinc.shared.domain.repository.UserRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val database: StorageDatabase,
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClient.client
) : UserRepository {

    override suspend fun isUsernameAvailable(username: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val count = client.postgrest["users"].select {
                filter {
                    eq("username", username)
                }
                count(io.github.jan.supabase.postgrest.query.Count.EXACT)
            }.countOrNull() ?: 0
            count == 0L
        }
    }

    override suspend fun getUserProfile(uid: String): Result<User?> = withContext(Dispatchers.IO) {
        runCatching {
            // Try local DB first
            val localUser = database.userQueries.selectById(uid).executeAsOneOrNull()?.let { mapDbUser(it) }
            if (localUser != null) return@runCatching localUser

            // Fetch from network
            val user = client.postgrest["users"].select {
                filter {
                    eq("uid", uid)
                }
            }.decodeSingleOrNull<User>()

            // Cache to DB if found
            if (user != null) {
                database.userQueries.insertUser(mapDomainUser(user))
            }
            user
        }
    }

    override suspend fun searchUsers(query: String): Result<List<User>> = withContext(Dispatchers.IO) {
        runCatching {
            if (query.isBlank()) return@runCatching emptyList()

            client.postgrest["users"].select {
                 filter {
                    or {
                        ilike("username", "%$query%")
                        ilike("display_name", "%$query%")
                    }
                 }
                 limit(20)
            }.decodeList<User>()
        }
    }

    override suspend fun updateUserProfile(uid: String, updates: Map<String, Any?>): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            client.postgrest["users"].update(updates) {
                filter {
                    eq("uid", uid)
                }
            }

            // Re-fetch to update cache
            // Note: getUserProfile handles DB insertion
            // We call it here to force cache update.
            // But we can't easily force network fetch if cache hits.
            // So we should probably invalidate cache or update DB directly if possible.
            // Or just clear cache (delete user from DB)?
            // Deleting is safer to ensure fresh fetch next time.
            // But getUserProfile prefers cache.
            // So we should delete first.
            // But database queries don't seem to have deleteById?
            // User.sq has deleteAll.
            // Let's assume for now we don't have granular cache invalidation easily exposed
            // unless we add deleteUser(uid) to User.sq.
            // But we can insert directly if we fetch from network explicitly.

            // Let's fetch from network explicitly here.
            val user = client.postgrest["users"].select {
                filter {
                    eq("uid", uid)
                }
            }.decodeSingleOrNull<User>()

            if (user != null) {
                database.userQueries.insertUser(mapDomainUser(user))
            }
            true
        }
    }

    private fun constructAvatarUrl(path: String): String {
        if (path.startsWith("http")) return path
        val baseUrl = SynapseConfig.SUPABASE_URL
        val cleanBaseUrl = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
        return "$cleanBaseUrl/storage/v1/object/public/avatars/$path"
    }

    private fun mapDbUser(dbUser: com.synapse.social.studioasinc.shared.data.database.User): User {
        return User(
            uid = dbUser.id,
            username = dbUser.username,
            email = dbUser.email,
            displayName = dbUser.fullName,
            avatar = dbUser.avatarUrl?.let { constructAvatarUrl(it) },
            bio = dbUser.bio,
            website = dbUser.website,
            location = dbUser.location,
            isVerified = dbUser.isVerified,
            followersCount = dbUser.followersCount,
            followingCount = dbUser.followingCount,
            postsCount = dbUser.postsCount
        )
    }

    private fun mapDomainUser(user: User): com.synapse.social.studioasinc.shared.data.database.User {
        return com.synapse.social.studioasinc.shared.data.database.User(
            id = user.uid,
            username = user.username ?: "",
            email = user.email,
            fullName = user.displayName,
            avatarUrl = user.avatar,
            bio = user.bio,
            website = user.website,
            location = user.location,
            isVerified = user.isVerified,
            followersCount = user.followersCount,
            followingCount = user.followingCount,
            postsCount = user.postsCount
        )
    }
}
