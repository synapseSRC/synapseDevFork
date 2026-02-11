package com.synapse.social.studioasinc.shared.domain.model.state

import com.synapse.social.studioasinc.shared.domain.model.*

sealed class ProfileSectionState<T> {
    abstract val items: T
    abstract val isLoading: Boolean
    abstract val error: String?
    abstract val hasChanges: Boolean

    data class Content<T>(
        override val items: T,
        override val isLoading: Boolean = false,
        override val error: String? = null,
        override val hasChanges: Boolean = false
    ) : ProfileSectionState<T>()
}

data class ProfileSectionsState(
    val socialLinks: ProfileSectionState<List<SocialLink>> = ProfileSectionState.Content(emptyList()),
    val workHistory: ProfileSectionState<List<WorkExperience>> = ProfileSectionState.Content(emptyList()),
    val education: ProfileSectionState<List<Education>> = ProfileSectionState.Content(emptyList()),
    val interests: ProfileSectionState<List<Interest>> = ProfileSectionState.Content(emptyList()),
    val travel: ProfileSectionState<List<TravelPlace>> = ProfileSectionState.Content(emptyList()),
    val contactInfo: ProfileSectionState<ContactInfo> = ProfileSectionState.Content(ContactInfo()),
    val relationshipStatus: ProfileSectionState<RelationshipStatus> = ProfileSectionState.Content(RelationshipStatus.HIDDEN),
    val familyConnections: ProfileSectionState<List<FamilyConnection>> = ProfileSectionState.Content(emptyList())
)

data class PrivacyUiState(
    val settings: PrivacySettings = PrivacySettings(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)
