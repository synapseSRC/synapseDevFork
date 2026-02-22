package com.synapse.social.studioasinc.shared.domain.model

import kotlinx.serialization.Serializable

sealed class NotificationError : Exception() {
    data object NetworkError : NotificationError()
    data object Unauthorized : NotificationError()
    data object Unknown : NotificationError()
}

@Serializable
enum class NotificationMessageType {
    CUSTOM,
    FALLBACK
}

@Serializable
data class Notification(
    val id: String = "",
    val type: String = "",
    val actorName: String? = null,
    val actorAvatar: String? = null,
    val message: String? = null,
    val messageType: NotificationMessageType = NotificationMessageType.CUSTOM,
    val timestamp: String = "",
    val isRead: Boolean = false,
    val targetId: String? = null,

    // App fields
    val from: String? = null,
    val postId: String? = null,
    val commentId: String? = null,
    val appTimestamp: Long = 0
)
