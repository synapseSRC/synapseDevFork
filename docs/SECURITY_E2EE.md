# 🔐 End-to-End Encryption (E2EE)

Synapse Social implements End-to-End Encryption (E2EE) for messaging to ensure that only the communicating users can read the messages. Our implementation is based on the **Signal Protocol**, the industry standard for secure communication.

## 🏗️ Architecture

The E2EE logic is housed within the `shared` module to ensure consistency across platforms.

### Key Components

- **`SignalProtocolManager` (Shared Interface):** Defines the core operations for key generation, message encryption, and decryption.
- **`AndroidSignalProtocolManager` (Android Implementation):** Uses the `libsignal-android` library to implement the Signal Protocol.
- **`AndroidSignalStore` (Android Specific):** Handles the persistent storage of identity keys, pre-keys, and session records using **EncryptedSharedPreferences** for hardware-backed security.
- **`SignalRepository` (Shared):** Manages the communication with the Supabase backend to upload and fetch public key bundles (PreKeyBundles).

## 🔑 Key Management (X3DH)

Synapse uses the **Extended Triple Diffie-Hellman (X3DH)** key agreement protocol to establish a shared secret between two users who do not have an established session.

1.  **Identity Keys:** Each device generates a long-term Identity Key Pair on the first run.
2.  **Pre-Keys:** The app generates a set of one-time Pre-Keys and a Signed Pre-Key.
3.  **Supabase Storage:** The public portions of these keys are uploaded to Supabase via `SignalRepository`.
4.  **Bundle Exchange:** When User A wants to message User B, they fetch User B's "PreKey Bundle" from the server to establish a secure session.

## 🔄 Messaging (Double Ratchet)

Once a session is established via X3DH, messages are encrypted using the **Double Ratchet** algorithm. This provides:

-   **Forward Secrecy:** Past messages cannot be decrypted even if the long-term keys are compromised.
-   **Post-Compromise Security:** The protocol automatically heals and provides security for future messages even after a temporary compromise.

## 🛠️ Usage Example

### Initializing the Manager (Android)

```kotlin
val manager = AndroidSignalProtocolManager(context)
val keys = manager.generateIdentityAndKeys()
repository.uploadIdentityKeys(userId, keys = keys)
```

### Encrypting a Message

```kotlin
val bundle = repository.fetchPreKeyBundle(recipientId)
if (bundle != null) {
    manager.processPreKeyBundle(recipientId, bundle)
    val encrypted = manager.encryptMessage(recipientId, "Hello Synapse!".toByteArray())
    // ... send encrypted message
} else {
    // Handle case where recipient has no pre-key bundle (e.g., show an error)
}
```

### Decrypting a Message

```kotlin
val decrypted = manager.decryptMessage(senderId, encryptedMessage)
println(String(decrypted)) // Output: Hello Synapse!
```

## 🛡️ Security Considerations

-   **Storage:** On Android, all sensitive keys are stored in `EncryptedSharedPreferences`, which utilizes the Android Keystore System for encryption at rest.
-   **Registration ID:** A unique 14-bit identifier is generated per installation to distinguish between multiple devices of the same user.
-   **Trust on First Use (TOFU):** Currently, the app trusts the identity key received the first time it communicates with a user. Future updates will include identity key verification via QR codes.
