package com.synapse.social.studioasinc.shared.domain.model

sealed class NotificationError : Exception() {
    object NetworkError : NotificationError()
    object Unauthorized : NotificationError()
    data class Unknown(override val message: String) : NotificationError()
}
