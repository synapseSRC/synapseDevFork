package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.model

data class ProfileStats(
    val postCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0
) {
    fun formatCount(count: Int): String = when {
        count >= 1_000_000 -> "${(count / 1_000_000f).toInt()}M"
        count >= 1_000 -> "${(count / 1_000f).toInt()}K"
        else -> count.toString()
    }
}
