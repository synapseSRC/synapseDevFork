package com.synapse.social.studioasinc.shared.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException

class AndroidSecureStorage(private val context: Context, private val sharedPreferences: SharedPreferences? = null) : SecureStorage {

    private val prefs: SharedPreferences by lazy {
        sharedPreferences ?: try {
            // Use MasterKey.Builder for stronger key derivation as MasterKeys is deprecated.
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "secure_storage_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize encrypted storage", e)
        }
    }

    override fun save(key: String, value: String) {
        if (!prefs.edit().putString(key, value).commit()) {
            throw IOException("Failed to save key $key to secure storage")
        }
    }

    override fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    override fun clear(key: String) {
        if (!prefs.edit().remove(key).commit()) {
            throw IOException("Failed to clear key $key from secure storage")
        }
    }
}
