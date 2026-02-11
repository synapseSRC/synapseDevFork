package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.repository.ProfileSectionsRepository
import com.synapse.social.studioasinc.shared.domain.usecase.UploadMediaUseCase
import com.synapse.social.studioasinc.shared.data.model.MediaType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

class ProfileSectionsRepositoryImpl(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClient.client,
    private val uploadMediaUseCase: UploadMediaUseCase
) : ProfileSectionsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getFullProfile(userId: String): Flow<Result<UserProfile>> = flow {
        try {
            val result = client.from("users")
                .select(columns = Columns.raw("*")) {
                    filter { eq("uid", userId) }
                }
                .decodeSingleOrNull<JsonObject>()

            if (result != null) {
                val socialLinks = result["social_links"]?.let {
                    json.decodeFromJsonElement<List<SocialLink>>(it)
                } ?: emptyList()

                val workHistory = result["work_history"]?.let {
                    json.decodeFromJsonElement<List<WorkExperience>>(it)
                } ?: emptyList()

                val education = result["education"]?.let {
                    json.decodeFromJsonElement<List<Education>>(it)
                } ?: emptyList()

                val interests = result["interests"]?.let {
                    json.decodeFromJsonElement<List<Interest>>(it)
                } ?: emptyList()

                val travel = result["travel"]?.let {
                    json.decodeFromJsonElement<List<TravelPlace>>(it)
                } ?: emptyList()

                val contactInfo = result["contact_info"]?.let {
                    json.decodeFromJsonElement<ContactInfo>(it)
                } ?: ContactInfo()

                val relationshipStatusStr = result["relationship_status"]?.jsonPrimitive?.contentOrNull
                val relationshipStatus = relationshipStatusStr?.let {
                    try { RelationshipStatus.valueOf(it) } catch (e: Exception) { null }
                } ?: RelationshipStatus.HIDDEN

                val privacySettings = result["privacy_settings"]?.let {
                    json.decodeFromJsonElement<PrivacySettings>(it)
                } ?: PrivacySettings()

                val user = UserProfile(
                    uid = result["uid"]?.jsonPrimitive?.contentOrNull ?: userId,
                    username = result["username"]?.jsonPrimitive?.contentOrNull ?: "",
                    displayName = result["display_name"]?.jsonPrimitive?.contentOrNull
                        ?: result["nickname"]?.jsonPrimitive?.contentOrNull,
                    email = result["email"]?.jsonPrimitive?.contentOrNull,
                    bio = result["bio"]?.jsonPrimitive?.contentOrNull,
                    avatar = result["avatar"]?.jsonPrimitive?.contentOrNull,
                    profileCoverImage = result["profile_cover_image"]?.jsonPrimitive?.contentOrNull,
                    gender = Gender.fromString(result["gender"]?.jsonPrimitive?.contentOrNull),
                    region = result["region"]?.jsonPrimitive?.contentOrNull,
                    status = UserStatus.fromString(result["status"]?.jsonPrimitive?.contentOrNull),
                    followersCount = result["followers_count"]?.jsonPrimitive?.intOrNull ?: 0,
                    followingCount = result["following_count"]?.jsonPrimitive?.intOrNull ?: 0,
                    postsCount = result["posts_count"]?.jsonPrimitive?.intOrNull ?: 0
                )

                emit(Result.success(user))
            } else {
                emit(Result.failure(Exception("User not found")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateSocialLinks(userId: String, links: List<SocialLink>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonElement = json.encodeToJsonElement(kotlinx.serialization.serializer(), links)
                client.from("users").update(mapOf("social_links" to jsonElement)) {
                    filter { eq("uid", userId) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateWorkHistory(userId: String, history: List<WorkExperience>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonElement = json.encodeToJsonElement(kotlinx.serialization.serializer(), history)
                client.from("users").update(mapOf("work_history" to jsonElement)) {
                    filter { eq("uid", userId) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateEducation(userId: String, education: List<Education>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonElement = json.encodeToJsonElement(kotlinx.serialization.serializer(), education)
                client.from("users").update(mapOf("education" to jsonElement)) {
                    filter { eq("uid", userId) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateInterests(userId: String, interests: List<Interest>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonElement = json.encodeToJsonElement(kotlinx.serialization.serializer(), interests)
                client.from("users").update(mapOf("interests" to jsonElement)) {
                    filter { eq("uid", userId) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateTravel(userId: String, travel: List<TravelPlace>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonElement = json.encodeToJsonElement(kotlinx.serialization.serializer(), travel)
                client.from("users").update(mapOf("travel" to jsonElement)) {
                    filter { eq("uid", userId) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateContactInfo(userId: String, contactInfo: ContactInfo): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonElement = json.encodeToJsonElement(kotlinx.serialization.serializer(), contactInfo)
                client.from("users").update(mapOf("contact_info" to jsonElement)) {
                    filter { eq("uid", userId) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateRelationshipStatus(userId: String, status: RelationshipStatus): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("users").update(mapOf("relationship_status" to status.name)) {
                    filter { eq("uid", userId) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun getPrivacySettings(userId: String): Flow<Result<PrivacySettings>> = flow {
        try {
            val result = client.from("users")
                .select(columns = Columns.raw("privacy_settings")) {
                    filter { eq("uid", userId) }
                }
                .decodeSingleOrNull<JsonObject>()

            val settings = result?.get("privacy_settings")?.let {
                json.decodeFromJsonElement<PrivacySettings>(it)
            } ?: PrivacySettings()

            emit(Result.success(settings))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updatePrivacySettings(userId: String, settings: PrivacySettings): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonElement = json.encodeToJsonElement(kotlinx.serialization.serializer(), settings)
                client.from("users").update(mapOf("privacy_settings" to jsonElement)) {
                    filter { eq("uid", userId) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun getFamilyConnections(userId: String): Flow<Result<List<FamilyConnection>>> = flow {
        try {
            val result = client.from("family_connections")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<FamilyConnection>()

            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun addFamilyConnection(userId: String, connection: FamilyConnection): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val forward = connection.copy(userId = userId)
                val reverse = connection.copy(
                    id = UUID.randomUUID().toString(),
                    userId = connection.relatedUserId,
                    relatedUserId = userId
                )

                client.from("family_connections").insert(listOf(forward, reverse))
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun removeFamilyConnection(connectionId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("family_connections").delete {
                    filter { eq("id", connectionId) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateBasicInfo(
        userId: String,
        username: String,
        displayName: String,
        bio: String,
        gender: String,
        region: String?
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val updateData = mutableMapOf<String, String>()
                updateData["username"] = username
                updateData["display_name"] = displayName
                updateData["bio"] = bio
                updateData["gender"] = gender
                region?.let { updateData["region"] = it }

                client.from("users").update(updateData) {
                    filter { eq("uid", userId) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun uploadAvatar(userId: String, filePath: String): Result<String> {
        return uploadMediaUseCase(
            filePath = filePath,
            mediaType = MediaType.PHOTO,
            bucketName = SupabaseClient.BUCKET_USER_AVATARS,
            onProgress = {}
        )
    }

    override suspend fun uploadCover(userId: String, filePath: String): Result<String> {
        return uploadMediaUseCase(
            filePath = filePath,
            mediaType = MediaType.PHOTO,
            bucketName = SupabaseClient.BUCKET_USER_COVERS,
            onProgress = {}
        )
    }

    override suspend fun checkUsernameAvailability(username: String, currentUserId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = client.from("users")
                    .select(columns = Columns.raw("uid")) {
                        filter { eq("username", username) }
                    }
                    .decodeList<JsonObject>()

                if (result.isEmpty()) {
                    Result.success(true)
                } else {
                    val existingUserId = result.first()["uid"]?.jsonPrimitive?.contentOrNull
                    Result.success(existingUserId == currentUserId)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun syncUsernameChange(oldUsername: String, newUsername: String, userId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("usernames").delete {
                    filter { eq("username", oldUsername) }
                }

                val email = client.auth.currentUserOrNull()?.email

                val usernameData = mapOf(
                    "uid" to userId,
                    "email" to email,
                    "username" to newUsername
                )
                client.from("usernames").upsert(usernameData)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
