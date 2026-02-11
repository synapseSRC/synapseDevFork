package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ProfileSectionsRepository {
    // Profile Fetching
    fun getFullProfile(userId: String): Flow<Result<UserProfile>>

    // Section Updates
    suspend fun updateSocialLinks(userId: String, links: List<SocialLink>): Result<Unit>
    suspend fun updateWorkHistory(userId: String, history: List<WorkExperience>): Result<Unit>
    suspend fun updateEducation(userId: String, education: List<Education>): Result<Unit>
    suspend fun updateInterests(userId: String, interests: List<Interest>): Result<Unit>
    suspend fun updateTravel(userId: String, travel: List<TravelPlace>): Result<Unit>
    suspend fun updateContactInfo(userId: String, contactInfo: ContactInfo): Result<Unit>
    suspend fun updateRelationshipStatus(userId: String, status: RelationshipStatus): Result<Unit>

    // Privacy
    fun getPrivacySettings(userId: String): Flow<Result<PrivacySettings>>
    suspend fun updatePrivacySettings(userId: String, settings: PrivacySettings): Result<Unit>

    // Family Connections
    fun getFamilyConnections(userId: String): Flow<Result<List<FamilyConnection>>>
    suspend fun addFamilyConnection(userId: String, connection: FamilyConnection): Result<Unit>
    suspend fun removeFamilyConnection(connectionId: String): Result<Unit>

    // Legacy/Core Profile Updates (Migrated from EditProfileRepository)
    suspend fun updateBasicInfo(userId: String, username: String, displayName: String, bio: String, gender: String, region: String?): Result<Unit>
    suspend fun uploadAvatar(userId: String, filePath: String): Result<String>
    suspend fun uploadCover(userId: String, filePath: String): Result<String>
    suspend fun checkUsernameAvailability(username: String, currentUserId: String): Result<Boolean>
    suspend fun syncUsernameChange(oldUsername: String, newUsername: String, userId: String): Result<Unit>
}
