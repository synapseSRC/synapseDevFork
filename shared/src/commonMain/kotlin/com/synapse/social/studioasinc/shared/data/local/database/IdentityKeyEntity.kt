package com.synapse.social.studioasinc.shared.data.local.database

// import androidx.room.Entity
// import androidx.room.PrimaryKey

// @Entity(tableName = "identity_keys")
data class IdentityKeyEntity(
    // @PrimaryKey
    val userId: String,
    val publicKey: String,
    val privateKey: String, // In production, this should be encrypted at rest using OS Keychain, or only stored in Keychain, not DB.
    val registrationId: Int,
    val createdAt: Long
)
