package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.feature.profile.editprofile.photohistory

data class HistoryItem(
    val key: String,
    val userId: String,
    val imageUrl: String,
    val uploadDate: Long,
    val type: String
)

enum class PhotoType {
    PROFILE, COVER
}
