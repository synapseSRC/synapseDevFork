package com.synapse.social.studioasinc.shared.data.mapper

import com.synapse.social.studioasinc.shared.data.model.NotificationDto
import com.synapse.social.studioasinc.shared.domain.model.Notification
import com.synapse.social.studioasinc.shared.domain.model.NotificationMessageType
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

fun NotificationDto.toDomain(): Notification {
    val messageBody = body["en"]?.jsonPrimitive?.contentOrNull
    val messageType = if (messageBody != null) NotificationMessageType.CUSTOM else NotificationMessageType.FALLBACK

    return Notification(
        id = id,
        type = type,
        actorName = actor?.displayName,
        actorAvatar = actor?.avatar,
        message = messageBody,
        messageType = messageType,
        timestamp = createdAt,
        isRead = isRead,
        targetId = data?.get("target_id")?.jsonPrimitive?.contentOrNull
    )
}
