package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model

data class Notification(
    var from: String? = null,
    var message: String? = null,
    var type: String? = null,
    var postId: String? = null,
    var commentId: String? = null,
    var timestamp: Long = 0
)
