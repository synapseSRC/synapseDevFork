package com.synapse.social.studioasinc.shared.data.crypto

import com.synapse.social.studioasinc.shared.data.crypto.models.EncryptedMessage
import com.synapse.social.studioasinc.shared.data.crypto.models.PreKeyBundle
import com.synapse.social.studioasinc.shared.data.crypto.models.SignalIdentityKeys
import com.synapse.social.studioasinc.shared.data.crypto.models.SignalOneTimePreKey

/**
 * Manager interface for handling End-to-End Encryption (E2EE) using the Signal Protocol.
 *
 * This interface defines the core operations required for secure communication, including
 * identity key generation, session establishment via pre-key bundles, and message encryption/decryption.
 */
interface SignalProtocolManager {
    /**
     * Generates a new long-term Identity Key Pair and a Registration ID for the current device.
     * Also generates the initial Signed Pre-Key.
     *
     * @return The generated [SignalIdentityKeys] containing public portions and metadata.
     */
    suspend fun generateIdentityAndKeys(): SignalIdentityKeys

    /**
     * Generates a set of one-time Pre-Keys used for the X3DH handshake.
     *
     * @param startId The starting ID for the generated keys.
     * @param count The number of keys to generate.
     * @return A list of [SignalOneTimePreKey] to be uploaded to the server.
     */
    suspend fun generateOneTimePreKeys(startId: Int, count: Int): List<SignalOneTimePreKey>

    /**
     * Processes a recipient's [PreKeyBundle] to establish a secure session.
     * This must be called before encrypting the first message to a new recipient.
     *
     * @param userId The ID of the recipient user.
     * @param bundle The public key bundle fetched from the server.
     */
    suspend fun processPreKeyBundle(userId: String, bundle: PreKeyBundle)

    /**
     * Encrypts a message for a specific recipient.
     *
     * @param recipientId The ID of the recipient user.
     * @param message The raw message bytes to encrypt.
     * @return An [EncryptedMessage] containing the ciphertext and protocol metadata.
     */
    suspend fun encryptMessage(recipientId: String, message: ByteArray): EncryptedMessage

    /**
     * Decrypts an incoming message from a sender.
     *
     * @param senderId The ID of the sender user.
     * @param message The [EncryptedMessage] received.
     * @return The decrypted message bytes.
     */
    suspend fun decryptMessage(senderId: String, message: EncryptedMessage): ByteArray

    /**
     * Returns the local registration ID of this installation.
     */
    suspend fun getLocalRegistrationId(): Int

    /**
     * Returns the Base64 encoded public identity key of this device.
     */
    suspend fun getLocalIdentityKey(): String
}
