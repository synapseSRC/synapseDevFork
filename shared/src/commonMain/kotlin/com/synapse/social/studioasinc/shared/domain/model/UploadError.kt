package com.synapse.social.studioasinc.shared.domain.model

sealed class UploadError : Exception() {
    data class CloudinaryError(override val message: String) : UploadError()
    data class Unknown(override val message: String) : UploadError()
}
