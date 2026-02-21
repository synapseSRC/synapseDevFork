package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.domain.repository.ProfileRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.Serializable
import io.github.jan.supabase.SupabaseClient as SupabaseClientType
import kotlinx.serialization.json.*

class ProfileRepositoryImpl(private val client: SupabaseClientType) : ProfileRepository {

    override suspend fun updateProfile(userId: String, profile: UserProfile): Result<UserProfile> = try {
        client.from("users").update(profile) {
            filter { eq("uid", userId) }
        }
        Result.success(profile)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun followUser(userId: String): Result<Unit> = try {
        val currentUserId = client.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
        client.from("follows").insert(mapOf("follower_id" to currentUserId, "following_id" to userId))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun unfollowUser(userId: String): Result<Unit> = try {
        val currentUserId = client.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
        client.from("follows").delete {
            filter { eq("follower_id", currentUserId); eq("following_id", userId) }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getFollowing(userId: String, limit: Int, offset: Int): Result<List<UserProfile>> = try {
        val result = client.from("follows")
            .select(columns = Columns.raw("*, users!following_id(*)")) {
                filter { eq("follower_id", userId) }
                limit(limit.toLong())
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<JsonObject>()

        val users = result.mapNotNull { it["users"]?.jsonObject?.let { userJson ->
            // Simple mapping for now
            UserProfile(
                uid = userJson["uid"]?.jsonPrimitive?.content ?: "",
                username = userJson["username"]?.jsonPrimitive?.content ?: ""
            )
        }}
        Result.success(users)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getProfilePosts(userId: String, limit: Int, offset: Int): Result<List<Post>> = try {
        val result = client.from("posts").select {
            filter { eq("author_uid", userId) }
            limit(limit.toLong())
            range(offset.toLong(), (offset + limit - 1).toLong())
            order("created_at", Order.DESCENDING)
        }.decodeList<JsonObject>()
        // Simple mapping
        Result.success(emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getProfilePhotos(userId: String, limit: Int, offset: Int): Result<List<MediaItem>> = Result.success(emptyList())
    override suspend fun getProfileReels(userId: String, limit: Int, offset: Int): Result<List<Reel>> = Result.success(emptyList())

    override suspend fun getProfile(userId: String): Result<UserProfile?> = try {
        val result = client.from("users").select {
            filter { eq("uid", userId) }
        }.decodeSingleOrNull<UserProfile>()
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun isFollowing(userId: String): Result<Boolean> = try {
        val currentUserId = client.auth.currentUserOrNull()?.id ?: return Result.success(false)
        val count = client.from("follows").select {
            filter { eq("follower_id", currentUserId); eq("following_id", userId) }
        }.decodeList<JsonObject>().size
        Result.success(count > 0)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun archiveProfile(userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun blockUser(userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun lockProfile(userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun muteUser(userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun reportUser(userId: String, reason: String): Result<Unit> = Result.success(Unit)
}
