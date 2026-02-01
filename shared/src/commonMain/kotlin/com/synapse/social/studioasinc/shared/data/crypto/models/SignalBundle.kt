package com.synapse.social.studioasinc.shared.data.crypto.models

import kotlinx.serialization.Serializable

@Serializable
data class SignalIdentityKeys(
    val registrationId: Int,
    val identityKey: String, // Base64
    val signedPreKeyId: Int,
    val signedPreKey: String, // Base64
    val signedPreKeySignature: String // Base64
)

@Serializable
data class SignalOneTimePreKey(
    val keyId: Int,
    val publicKey: String // Base64
)

@Serializable
data class PreKeyBundle(
    val registrationId: Int,
    val deviceId: Int,
    val preKeyId: Int?,
    val preKeyPublic: String?, // Base64
    val signedPreKeyId: Int,
    val signedPreKeyPublic: String, // Base64
    val signedPreKeySignature: String, // Base64
    val identityKey: String // Base64
)

@Serializable
data class EncryptedMessage(
    val type: Int, // 3 for PreKeyWhisperMessage, 1 for WhisperMessage
    val body: String, // Base64
    val registrationId: Int
)
