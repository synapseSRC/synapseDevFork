package com.synapse.social.studioasinc.shared.domain.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement



@Serializable
data class PendingAction(
    val id: String,
    val actionType: ActionType,
    val messageId: String,
    val parameters: Map<String, JsonElement>,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val retryCount: Int = 0
) {
    @Serializable
    enum class ActionType {
        EDIT,
        DELETE,
        FORWARD
    }
}
