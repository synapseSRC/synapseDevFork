package com.synapse.social.studioasinc.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SocialPlatform {
    FACEBOOK, INSTAGRAM, TWITTER, LINKEDIN, TIKTOK, YOUTUBE, SNAPCHAT, GITHUB, WEBSITE, OTHER
}

@Serializable
data class SocialLink(
    val id: String,
    val platform: SocialPlatform,
    val url: String,
    val displayOrder: Int = 0
)

@Serializable
data class WorkExperience(
    val id: String,
    val company: String,
    val position: String,
    val startDate: Long, // Epoch millis
    val endDate: Long? = null, // Null means "Present"
    val description: String? = null,
    val isCurrent: Boolean = false
)

@Serializable
data class Education(
    val id: String,
    val school: String,
    val degree: String? = null,
    val fieldOfStudy: String? = null,
    val startDate: Long,
    val endDate: Long? = null,
    val description: String? = null
)

@Serializable
data class Interest(
    val id: String,
    val name: String,
    val category: String? = null
)

@Serializable
data class FamilyConnection(
    val id: String,
    val userId: String, // The user ID of the family member
    val relatedUserId: String, // The connected user ID
    val relationshipType: String, // e.g., "Mother", "Sibling"
    val privacyLevel: PrivacyLevel = PrivacyLevel.FRIENDS
)

@Serializable
enum class RelationshipStatus {
    SINGLE, IN_A_RELATIONSHIP, ENGAGED, MARRIED, DIVORCED, WIDOWED, SEPARATED, COMPLICATED, HIDDEN
}

@Serializable
data class TravelPlace(
    val id: String,
    val placeName: String,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val description: String? = null
)

@Serializable
data class ContactInfo(
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val website: String? = null
)

@Serializable
enum class PrivacyLevel {
    PUBLIC, FRIENDS, ONLY_ME
}

@Serializable
sealed class ProfileSection {
    abstract val title: String
    abstract val privacyLevel: PrivacyLevel
}

@Serializable
data class PrivacySettings(
    val sectionDefaults: Map<String, PrivacyLevel> = emptyMap(),
    val itemOverrides: Map<String, PrivacyLevel> = emptyMap()
)
