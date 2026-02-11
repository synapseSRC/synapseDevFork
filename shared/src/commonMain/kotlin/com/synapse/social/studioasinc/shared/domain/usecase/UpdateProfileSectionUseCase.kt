package com.synapse.social.studioasinc.shared.domain.usecase

import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.repository.ProfileSectionsRepository

class UpdateProfileSectionUseCase(
    private val repository: ProfileSectionsRepository
) {
    suspend fun updateSocialLinks(userId: String, links: List<SocialLink>): Result<Unit> =
        repository.updateSocialLinks(userId, links)

    suspend fun updateWorkHistory(userId: String, history: List<WorkExperience>): Result<Unit> =
        repository.updateWorkHistory(userId, history)

    suspend fun updateEducation(userId: String, education: List<Education>): Result<Unit> =
        repository.updateEducation(userId, education)

    suspend fun updateInterests(userId: String, interests: List<Interest>): Result<Unit> =
        repository.updateInterests(userId, interests)

    suspend fun updateTravel(userId: String, travel: List<TravelPlace>): Result<Unit> =
        repository.updateTravel(userId, travel)

    suspend fun updateContactInfo(userId: String, contactInfo: ContactInfo): Result<Unit> =
        repository.updateContactInfo(userId, contactInfo)

    suspend fun updateRelationshipStatus(userId: String, status: RelationshipStatus): Result<Unit> =
        repository.updateRelationshipStatus(userId, status)

    suspend fun updateBasicInfo(userId: String, username: String, displayName: String, bio: String, gender: String, region: String?): Result<Unit> =
        repository.updateBasicInfo(userId, username, displayName, bio, gender, region)
}
