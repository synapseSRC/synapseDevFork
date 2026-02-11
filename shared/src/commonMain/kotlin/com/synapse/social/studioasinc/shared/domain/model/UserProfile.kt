package com.synapse.social.studioasinc.shared.domain.model

import kotlinx.serialization.Serializable

// Aligning with existing User.kt but as a clean Domain Model if User.kt is a Data Model
// However, User.kt seems to be the main domain model used elsewhere.
// To fix the immediate error, I will alias UserProfile to User or create a compatible one.
// Given existing code uses User, I should probably use User.
// But the prompt specified creating new things.
// Let's create a typealias or wrapper if needed, but easiest is to define UserProfile matching expectation if it doesn't exist.
// Wait, User.kt HAS most fields.
// I will create UserProfile.kt as a data class if it's missing, OR refactor code to use User.
// Refactoring to use User is better for consistency.
// BUT, I'll create UserProfile to match my new repository code to minimize changes to the "User" class which might break other things.

@Serializable
data class UserProfile(
    val uid: String,
    val username: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val bio: String? = null,
    val avatar: String? = null,
    val profileCoverImage: String? = null,
    val gender: Gender? = null,
    val region: String? = null,
    val status: UserStatus? = null,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    // Extended fields (optional, to avoid breaking serialization if not present in basic fetch)
    val socialLinks: List<SocialLink> = emptyList(),
    val workHistory: List<WorkExperience> = emptyList(),
    val education: List<Education> = emptyList(),
    val interests: List<Interest> = emptyList(),
    val travel: List<TravelPlace> = emptyList(),
    val contactInfo: ContactInfo? = null,
    val relationshipStatus: RelationshipStatus? = null,
    val privacySettings: PrivacySettings? = null
)

@Serializable
enum class Gender {
    Male, Female, Hidden, Other;

    companion object {
        fun fromString(value: String?): Gender {
            return when (value?.lowercase()) {
                "male" -> Male
                "female" -> Female
                "other" -> Other
                else -> Hidden
            }
        }
    }
}
