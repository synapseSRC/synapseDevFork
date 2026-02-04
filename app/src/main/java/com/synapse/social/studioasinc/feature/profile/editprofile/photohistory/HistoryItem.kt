package com.synapse.social.studioasinc.feature.profile.editprofile.photohistory

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
