package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.data.crypto.models.PreKeyBundle
import com.synapse.social.studioasinc.shared.data.crypto.models.SignalIdentityKeys
import com.synapse.social.studioasinc.shared.data.crypto.models.SignalOneTimePreKey
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Repository responsible for managing the synchronization of Signal Protocol public keys
 * with the Supabase backend.
 *
 * This repository handles uploading identity keys and one-time pre-keys, as well as
 * fetching public key bundles required to establish encrypted sessions with other users.
 */
class SignalRepository(private val supabase: SupabaseClient) {

    @Serializable
    data class IdentityKeyDto(
        @SerialName("user_id") val userId: String? = null,
        @SerialName("device_id") val deviceId: Int,
        @SerialName("registration_id") val registrationId: Int,
        @SerialName("identity_key") val identityKey: String,
        @SerialName("signed_pre_key_id") val signedPreKeyId: Int,
        @SerialName("signed_pre_key") val signedPreKey: String,
        @SerialName("signed_pre_key_signature") val signedPreKeySignature: String
    )

    @Serializable
    data class PreKeyDto(
        @SerialName("user_id") val userId: String? = null,
        @SerialName("key_id") val keyId: Int,
        @SerialName("public_key") val publicKey: String
    )

    /**
     * Uploads the public identity keys for the current user and device to the server.
     *
     * @param userId The ID of the current user.
     * @param deviceId The ID of the current device.
     * @param keys The [SignalIdentityKeys] to upload.
     */
    suspend fun uploadIdentityKeys(userId: String, deviceId: Int = 1, keys: SignalIdentityKeys) {
        val dto = IdentityKeyDto(
            userId = userId,
            deviceId = deviceId,
            registrationId = keys.registrationId,
            identityKey = keys.identityKey,
            signedPreKeyId = keys.signedPreKeyId,
            signedPreKey = keys.signedPreKey,
            signedPreKeySignature = keys.signedPreKeySignature
        )
        supabase.postgrest.from("signal_identity_keys").upsert(dto)
    }

    /**
     * Uploads a list of one-time public pre-keys for the current user to the server.
     *
     * @param userId The ID of the current user.
     * @param keys The list of [SignalOneTimePreKey] to upload.
     */
    suspend fun uploadOneTimePreKeys(userId: String, keys: List<SignalOneTimePreKey>) {
        val dtos = keys.map { key ->
            PreKeyDto(
                userId = userId,
                keyId = key.keyId,
                publicKey = key.publicKey
            )
        }
        supabase.postgrest.from("signal_one_time_pre_keys").insert(dtos)
    }

    /**
     * Fetches the [PreKeyBundle] for a specific user to initiate a secure session.
     *
     * This method fetches the user's identity keys and claims a one-time pre-key
     * from the server via a stored procedure.
     *
     * @param userId The ID of the user whose bundle is being fetched.
     * @return The [PreKeyBundle] if successful, or null if the user has no keys registered.
     */
    suspend fun fetchPreKeyBundle(userId: String): PreKeyBundle? {
        // Fetch Identity Key (Currently fetching for default device 1, logic needs to expand for multi-device)
        val identityKeyResult = try {
            supabase.postgrest.from("signal_identity_keys")
                .select(columns = Columns.list("user_id", "device_id", "registration_id", "identity_key", "signed_pre_key_id", "signed_pre_key", "signed_pre_key_signature")) {
                    filter {
                        eq("user_id", userId)
                    }
                    limit(1) // Just get one device for now
                }.decodeSingleOrNull<IdentityKeyDto>()
        } catch (e: Exception) {
            Napier.e("Failed to fetch identity key for user $userId", e)
            return null
        }

        if (identityKeyResult == null) return null

        // Fetch One-Time PreKey (Claim it)
        // Using RPC 'claim_one_time_pre_key'
        val preKeyResult = try {
            supabase.postgrest.rpc("claim_one_time_pre_key", mapOf("target_user_id" to userId))
                .decodeAsOrNull<PreKeyDto>()
        } catch (e: Exception) {
            Napier.e("Failed to claim one-time pre-key for user $userId", e)
            null
        }

        return PreKeyBundle(
            registrationId = identityKeyResult.registrationId,
            deviceId = identityKeyResult.deviceId,
            preKeyId = preKeyResult?.keyId,
            preKeyPublic = preKeyResult?.publicKey,
            signedPreKeyId = identityKeyResult.signedPreKeyId,
            signedPreKeyPublic = identityKeyResult.signedPreKey,
            signedPreKeySignature = identityKeyResult.signedPreKeySignature,
            identityKey = identityKeyResult.identityKey
        )
    }
}
