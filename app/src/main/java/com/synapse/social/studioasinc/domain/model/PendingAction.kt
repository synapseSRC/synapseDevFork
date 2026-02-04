package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a pending message action that needs to be executed when online
 */
@Serializable
data class PendingAction(
    val id: String,
    val actionType: ActionType,
    val messageId: String,
    val parameters: Map<String, JsonElement>,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
) {
    @Serializable
    enum class ActionType {
        EDIT,
        DELETE,
        FORWARD
    }
}
