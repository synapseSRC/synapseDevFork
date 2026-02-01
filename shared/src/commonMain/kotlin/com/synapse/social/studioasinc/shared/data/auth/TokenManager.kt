package com.synapse.social.studioasinc.shared.data.auth

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.Serializable
import com.synapse.social.studioasinc.shared.core.util.getCurrentTimeMillis

@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val userEmail: String,
    val expiryTime: Long
)

class TokenManager(private val settings: Settings) {

    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
        private const val KEY_USER_ID = "auth_user_id"
        private const val KEY_USER_EMAIL = "auth_user_email"
        private const val KEY_EXPIRY_TIME = "auth_token_expiry"
    }

    fun storeTokens(
        accessToken: String,
        refreshToken: String,
        userId: String,
        userEmail: String,
        expiresIn: Int
    ) {
        val expiryTime = getCurrentTimeMillis() + (expiresIn * 1000L)

        settings[KEY_ACCESS_TOKEN] = accessToken
        settings[KEY_REFRESH_TOKEN] = refreshToken
        settings[KEY_USER_ID] = userId
        settings[KEY_USER_EMAIL] = userEmail
        settings.putLong(KEY_EXPIRY_TIME, expiryTime)
    }

    fun getStoredTokens(): AuthTokens? {
        val accessToken = settings.getStringOrNull(KEY_ACCESS_TOKEN) ?: return null
        val refreshToken = settings.getStringOrNull(KEY_REFRESH_TOKEN) ?: ""
        val userId = settings.getStringOrNull(KEY_USER_ID) ?: ""
        val userEmail = settings.getStringOrNull(KEY_USER_EMAIL) ?: ""
        val expiryTime = settings.getLong(KEY_EXPIRY_TIME, 0L)

        return AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            userEmail = userEmail,
            expiryTime = expiryTime
        )
    }

    fun clearTokens() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_EMAIL)
        settings.remove(KEY_EXPIRY_TIME)
    }

    fun areTokensValid(): Boolean {
        val expiryTime = settings.getLong(KEY_EXPIRY_TIME, 0L)
        return expiryTime > getCurrentTimeMillis()
    }
}
