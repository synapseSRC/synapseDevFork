package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.local.database

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.AppearanceSettings
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.ChatSettings
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.ContentVisibility
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.FontScale
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaAutoDownload
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.NotificationCategory
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.NotificationPreferences
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.PrivacySettings
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.ProfileVisibility
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.ThemeMode
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model.ChatThemePreset
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model.ChatWallpaper
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model.WallpaperType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * DataStore singleton for user settings.
 * Uses a separate DataStore from AppSettingsManager to keep settings organized.
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "synapse_user_settings"
)

/**
 * DataStore implementation for persisting user settings.
 *
 * This class handles all settings persistence using Android DataStore Preferences.
 * It provides Flow-based reactive access to settings and handles errors gracefully
 * by returning default values when reads fail.
 *
 * Requirements: 10.1, 10.2, 10.4
 */
class SettingsDataStore private constructor(private val context: Context) {

    companion object {
        private const val TAG = "SettingsDataStore"

        @Volatile
        private var INSTANCE: SettingsDataStore? = null

        fun getInstance(context: Context): SettingsDataStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsDataStore(context.applicationContext).also { INSTANCE = it }
            }
        }


        // ====================================================================
        // Device-level Settings Keys (preserved on logout)
        // ====================================================================

        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
        private val KEY_FONT_SCALE = stringPreferencesKey("font_scale")
        private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_POST_VIEW_STYLE = stringPreferencesKey("post_view_style")

        // ====================================================================
        // User-level Settings Keys (cleared on logout)
        // ====================================================================

        // Privacy Settings
        private val KEY_PROFILE_VISIBILITY = stringPreferencesKey("profile_visibility")
        private val KEY_CONTENT_VISIBILITY = stringPreferencesKey("content_visibility")
        private val KEY_BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
        private val KEY_TWO_FACTOR_ENABLED = booleanPreferencesKey("two_factor_enabled")

        // Notification Settings
        private val KEY_NOTIFICATIONS_LIKES = booleanPreferencesKey("notifications_likes")
        private val KEY_NOTIFICATIONS_COMMENTS = booleanPreferencesKey("notifications_comments")
        private val KEY_NOTIFICATIONS_FOLLOWS = booleanPreferencesKey("notifications_follows")
        private val KEY_NOTIFICATIONS_MESSAGES = booleanPreferencesKey("notifications_messages")
        private val KEY_NOTIFICATIONS_MENTIONS = booleanPreferencesKey("notifications_mentions")
        private val KEY_IN_APP_NOTIFICATIONS = booleanPreferencesKey("in_app_notifications")

        // Chat Settings
        private val KEY_READ_RECEIPTS_ENABLED = booleanPreferencesKey("read_receipts_enabled")
        private val KEY_TYPING_INDICATORS_ENABLED = booleanPreferencesKey("typing_indicators_enabled")
        private val KEY_MEDIA_AUTO_DOWNLOAD = stringPreferencesKey("media_auto_download")
        private val KEY_CHAT_FONT_SCALE = floatPreferencesKey("chat_font_scale")
        private val KEY_CHAT_THEME_PRESET = stringPreferencesKey("chat_theme_preset")
        private val KEY_CHAT_WALLPAPER_TYPE = stringPreferencesKey("chat_wallpaper_type")
        private val KEY_CHAT_WALLPAPER_VALUE = stringPreferencesKey("chat_wallpaper_value")

        // Data Saver
        private val KEY_DATA_SAVER_ENABLED = booleanPreferencesKey("data_saver_enabled")

        // Additional Chat Settings
        private val KEY_ENTER_IS_SEND_ENABLED = booleanPreferencesKey("enter_is_send_enabled")
        private val KEY_MEDIA_VISIBILITY_ENABLED = booleanPreferencesKey("media_visibility_enabled")
        private val KEY_VOICE_TRANSCRIPTS_ENABLED = booleanPreferencesKey("voice_transcripts_enabled")
        private val KEY_AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")

        // Additional Notification Settings
        private val KEY_REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        private val KEY_HIGH_PRIORITY_ENABLED = booleanPreferencesKey("high_priority_enabled")
        private val KEY_REACTION_NOTIFICATIONS_ENABLED = booleanPreferencesKey("reaction_notifications_enabled")

        // Additional Privacy Settings
        private val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        private val KEY_CHAT_LOCK_ENABLED = booleanPreferencesKey("chat_lock_enabled")

        // Storage and Data Settings
        private val KEY_MEDIA_UPLOAD_QUALITY = stringPreferencesKey("media_upload_quality")
        private val KEY_USE_LESS_DATA_CALLS = booleanPreferencesKey("use_less_data_calls")
        private val KEY_AUTO_DOWNLOAD_MOBILE = androidx.datastore.preferences.core.stringSetPreferencesKey("auto_download_mobile")
        private val KEY_AUTO_DOWNLOAD_WIFI = androidx.datastore.preferences.core.stringSetPreferencesKey("auto_download_wifi")
        private val KEY_AUTO_DOWNLOAD_ROAMING = androidx.datastore.preferences.core.stringSetPreferencesKey("auto_download_roaming")

        // Request Account Info Settings
        private val KEY_ACCOUNT_REPORTS_AUTO_CREATE = booleanPreferencesKey("account_reports_auto_create")
        private val KEY_CHANNELS_REPORTS_AUTO_CREATE = booleanPreferencesKey("channels_reports_auto_create")

        // ====================================================================
        // Default Values
        // ====================================================================

        val DEFAULT_THEME_MODE = ThemeMode.SYSTEM
        val DEFAULT_DYNAMIC_COLOR_ENABLED = true
        val DEFAULT_FONT_SCALE = FontScale.MEDIUM
        val DEFAULT_APP_LANGUAGE = "en"
        val DEFAULT_PROFILE_VISIBILITY = ProfileVisibility.PUBLIC
        val DEFAULT_CONTENT_VISIBILITY = ContentVisibility.EVERYONE
        val DEFAULT_BIOMETRIC_LOCK_ENABLED = false
        val DEFAULT_TWO_FACTOR_ENABLED = false
        val DEFAULT_NOTIFICATIONS_ENABLED = true
        val DEFAULT_IN_APP_NOTIFICATIONS_ENABLED = true
        val DEFAULT_READ_RECEIPTS_ENABLED = true
        val DEFAULT_TYPING_INDICATORS_ENABLED = true
        val DEFAULT_MEDIA_AUTO_DOWNLOAD = MediaAutoDownload.WIFI_ONLY
        val DEFAULT_CHAT_FONT_SCALE = 1.0f
        val DEFAULT_CHAT_THEME_PRESET = ChatThemePreset.DEFAULT
        val DEFAULT_CHAT_WALLPAPER_TYPE = WallpaperType.DEFAULT
        val DEFAULT_DATA_SAVER_ENABLED = false
        val DEFAULT_ENTER_IS_SEND_ENABLED = false
        val DEFAULT_MEDIA_VISIBILITY_ENABLED = true
        val DEFAULT_VOICE_TRANSCRIPTS_ENABLED = false
        val DEFAULT_AUTO_BACKUP_ENABLED = true
        val DEFAULT_REMINDERS_ENABLED = false
        val DEFAULT_HIGH_PRIORITY_ENABLED = true
        val DEFAULT_REACTION_NOTIFICATIONS_ENABLED = true
        val DEFAULT_APP_LOCK_ENABLED = false
        val DEFAULT_CHAT_LOCK_ENABLED = false
        val DEFAULT_ACCOUNT_REPORTS_AUTO_CREATE = false
        val DEFAULT_CHANNELS_REPORTS_AUTO_CREATE = false
    }

    private val dataStore: DataStore<Preferences>
        get() = context.settingsDataStore


    // ========================================================================
    // Safe Read Helper
    // ========================================================================

    /**
     * Creates a Flow that handles DataStore read errors gracefully.
     * On IOException, logs the error and emits empty preferences (triggering defaults).
     * Requirements: 10.4
     */
    private fun safePreferencesFlow(): Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    // ========================================================================
    // Theme Settings (Device-level)
    // ========================================================================

    /**
     * Flow of the current theme mode.
     * Returns DEFAULT_THEME_MODE if read fails or value not set.
     */
    val themeMode: Flow<ThemeMode> = safePreferencesFlow().map { preferences ->
        preferences[KEY_THEME_MODE]?.let { value ->
            runCatching { ThemeMode.valueOf(value) }.getOrDefault(DEFAULT_THEME_MODE)
        } ?: DEFAULT_THEME_MODE
    }

    /**
     * Flow of dynamic color enabled state.
     * Returns DEFAULT_DYNAMIC_COLOR_ENABLED if read fails or value not set.
     */
    val dynamicColorEnabled: Flow<Boolean> = safePreferencesFlow().map { preferences ->
        preferences[KEY_DYNAMIC_COLOR_ENABLED] ?: DEFAULT_DYNAMIC_COLOR_ENABLED
    }

    /**
     * Flow of font scale setting.
     * Returns DEFAULT_FONT_SCALE if read fails or value not set.
     */
    val fontScale: Flow<FontScale> = safePreferencesFlow().map { preferences ->
        preferences[KEY_FONT_SCALE]?.let { value ->
            runCatching { FontScale.valueOf(value) }.getOrDefault(DEFAULT_FONT_SCALE)
        } ?: DEFAULT_FONT_SCALE
    }

    /**
     * Flow of combined appearance settings.
     */
    val appearanceSettings: Flow<AppearanceSettings> = safePreferencesFlow().map { preferences ->
        AppearanceSettings(
            themeMode = preferences[KEY_THEME_MODE]?.let { value ->
                runCatching { ThemeMode.valueOf(value) }.getOrDefault(DEFAULT_THEME_MODE)
            } ?: DEFAULT_THEME_MODE,
            dynamicColorEnabled = preferences[KEY_DYNAMIC_COLOR_ENABLED] ?: DEFAULT_DYNAMIC_COLOR_ENABLED,
            fontScale = preferences[KEY_FONT_SCALE]?.let { value ->
                runCatching { FontScale.valueOf(value) }.getOrDefault(DEFAULT_FONT_SCALE)
            } ?: DEFAULT_FONT_SCALE,
            postViewStyle = preferences[KEY_POST_VIEW_STYLE]?.let { value ->
                runCatching { com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.PostViewStyle.valueOf(value) }
                    .getOrDefault(com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.PostViewStyle.SWIPE)
            } ?: com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.PostViewStyle.SWIPE
        )
    }

    /**
     * Sets the theme mode.
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    /**
     * Sets dynamic color enabled state.
     */
    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DYNAMIC_COLOR_ENABLED] = enabled
        }
    }

    /**
     * Sets font scale.
     */
    suspend fun setFontScale(scale: FontScale) {
        dataStore.edit { preferences ->
            preferences[KEY_FONT_SCALE] = scale.name
        }
    }

    /**
     * Sets post view style.
     */
    suspend fun setPostViewStyle(style: com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.PostViewStyle) {
        dataStore.edit { preferences ->
            preferences[KEY_POST_VIEW_STYLE] = style.name
        }
    }

    /**
     * Flow of the current language code.
     * Returns DEFAULT_APP_LANGUAGE if read fails or value not set.
     */
    val language: Flow<String> = safePreferencesFlow().map { preferences ->
        preferences[KEY_APP_LANGUAGE] ?: DEFAULT_APP_LANGUAGE
    }

    /**
     * Sets the app language code.
     */
    suspend fun setLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_APP_LANGUAGE] = languageCode
        }
    }


    // ========================================================================
    // Privacy Settings (User-level)
    // ========================================================================

    /**
     * Flow of profile visibility setting.
     */
    val profileVisibility: Flow<ProfileVisibility> = safePreferencesFlow().map { preferences ->
        preferences[KEY_PROFILE_VISIBILITY]?.let { value ->
            runCatching { ProfileVisibility.valueOf(value) }.getOrDefault(DEFAULT_PROFILE_VISIBILITY)
        } ?: DEFAULT_PROFILE_VISIBILITY
    }

    /**
     * Flow of content visibility setting.
     */
    val contentVisibility: Flow<ContentVisibility> = safePreferencesFlow().map { preferences ->
        preferences[KEY_CONTENT_VISIBILITY]?.let { value ->
            runCatching { ContentVisibility.valueOf(value) }.getOrDefault(DEFAULT_CONTENT_VISIBILITY)
        } ?: DEFAULT_CONTENT_VISIBILITY
    }

    /**
     * Flow of biometric lock enabled state.
     */
    val biometricLockEnabled: Flow<Boolean> = safePreferencesFlow().map { preferences ->
        preferences[KEY_BIOMETRIC_LOCK_ENABLED] ?: DEFAULT_BIOMETRIC_LOCK_ENABLED
    }

    /**
     * Flow of two-factor authentication enabled state.
     */
    val twoFactorEnabled: Flow<Boolean> = safePreferencesFlow().map { preferences ->
        preferences[KEY_TWO_FACTOR_ENABLED] ?: DEFAULT_TWO_FACTOR_ENABLED
    }

    /**
     * Flow of combined privacy settings.
     */
    val privacySettings: Flow<PrivacySettings> = safePreferencesFlow().map { preferences ->
        PrivacySettings(
            profileVisibility = preferences[KEY_PROFILE_VISIBILITY]?.let { value ->
                runCatching { ProfileVisibility.valueOf(value) }.getOrDefault(DEFAULT_PROFILE_VISIBILITY)
            } ?: DEFAULT_PROFILE_VISIBILITY,
            twoFactorEnabled = preferences[KEY_TWO_FACTOR_ENABLED] ?: DEFAULT_TWO_FACTOR_ENABLED,
            biometricLockEnabled = preferences[KEY_BIOMETRIC_LOCK_ENABLED] ?: DEFAULT_BIOMETRIC_LOCK_ENABLED,
            contentVisibility = preferences[KEY_CONTENT_VISIBILITY]?.let { value ->
                runCatching { ContentVisibility.valueOf(value) }.getOrDefault(DEFAULT_CONTENT_VISIBILITY)
            } ?: DEFAULT_CONTENT_VISIBILITY
        )
    }

    /**
     * Sets profile visibility.
     */
    suspend fun setProfileVisibility(visibility: ProfileVisibility) {
        dataStore.edit { preferences ->
            preferences[KEY_PROFILE_VISIBILITY] = visibility.name
        }
    }

    /**
     * Sets content visibility.
     */
    suspend fun setContentVisibility(visibility: ContentVisibility) {
        dataStore.edit { preferences ->
            preferences[KEY_CONTENT_VISIBILITY] = visibility.name
        }
    }

    /**
     * Sets biometric lock enabled state.
     */
    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_BIOMETRIC_LOCK_ENABLED] = enabled
        }
    }

    /**
     * Sets two-factor authentication enabled state.
     */
    suspend fun setTwoFactorEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_TWO_FACTOR_ENABLED] = enabled
        }
    }


    // ========================================================================
    // Notification Settings (User-level)
    // ========================================================================

    /**
     * Flow of notification preferences.
     */
    val notificationPreferences: Flow<NotificationPreferences> = safePreferencesFlow().map { preferences ->
        NotificationPreferences(
            likesEnabled = preferences[KEY_NOTIFICATIONS_LIKES] ?: DEFAULT_NOTIFICATIONS_ENABLED,
            commentsEnabled = preferences[KEY_NOTIFICATIONS_COMMENTS] ?: DEFAULT_NOTIFICATIONS_ENABLED,
            followsEnabled = preferences[KEY_NOTIFICATIONS_FOLLOWS] ?: DEFAULT_NOTIFICATIONS_ENABLED,
            messagesEnabled = preferences[KEY_NOTIFICATIONS_MESSAGES] ?: DEFAULT_NOTIFICATIONS_ENABLED,
            mentionsEnabled = preferences[KEY_NOTIFICATIONS_MENTIONS] ?: DEFAULT_NOTIFICATIONS_ENABLED,
            inAppNotificationsEnabled = preferences[KEY_IN_APP_NOTIFICATIONS] ?: DEFAULT_IN_APP_NOTIFICATIONS_ENABLED
        )
    }

    /**
     * Updates notification preference for a specific category.
     */
    suspend fun updateNotificationPreference(category: NotificationCategory, enabled: Boolean) {
        dataStore.edit { preferences ->
            when (category) {
                NotificationCategory.LIKES -> preferences[KEY_NOTIFICATIONS_LIKES] = enabled
                NotificationCategory.COMMENTS -> preferences[KEY_NOTIFICATIONS_COMMENTS] = enabled
                NotificationCategory.FOLLOWS -> preferences[KEY_NOTIFICATIONS_FOLLOWS] = enabled
                NotificationCategory.MESSAGES -> preferences[KEY_NOTIFICATIONS_MESSAGES] = enabled
                NotificationCategory.MENTIONS -> preferences[KEY_NOTIFICATIONS_MENTIONS] = enabled
                NotificationCategory.REPLIES,
                NotificationCategory.NEW_POSTS,
                NotificationCategory.SHARES,
                NotificationCategory.SYSTEM_UPDATES -> {
                    // These are handled by Supabase sync in the new system
                }
            }
        }
    }

    /**
     * Sets in-app notifications enabled state.
     */
    suspend fun setInAppNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IN_APP_NOTIFICATIONS] = enabled
        }
    }

    // ========================================================================
    // Chat Settings (User-level)
    // ========================================================================

    /**
     * Flow of chat settings.
     */
    val chatSettings: Flow<ChatSettings> = safePreferencesFlow().map { preferences ->
        ChatSettings(
            readReceiptsEnabled = preferences[KEY_READ_RECEIPTS_ENABLED] ?: DEFAULT_READ_RECEIPTS_ENABLED,
            typingIndicatorsEnabled = preferences[KEY_TYPING_INDICATORS_ENABLED] ?: DEFAULT_TYPING_INDICATORS_ENABLED,
            mediaAutoDownload = preferences[KEY_MEDIA_AUTO_DOWNLOAD]?.let { value ->
                runCatching { MediaAutoDownload.valueOf(value) }.getOrDefault(DEFAULT_MEDIA_AUTO_DOWNLOAD)
            } ?: DEFAULT_MEDIA_AUTO_DOWNLOAD,
            chatFontScale = preferences[KEY_CHAT_FONT_SCALE] ?: DEFAULT_CHAT_FONT_SCALE,
            themePreset = preferences[KEY_CHAT_THEME_PRESET]?.let { value ->
                runCatching { ChatThemePreset.valueOf(value) }.getOrDefault(DEFAULT_CHAT_THEME_PRESET)
            } ?: DEFAULT_CHAT_THEME_PRESET,
            wallpaper = ChatWallpaper(
                type = preferences[KEY_CHAT_WALLPAPER_TYPE]?.let { value ->
                    runCatching { WallpaperType.valueOf(value) }.getOrDefault(DEFAULT_CHAT_WALLPAPER_TYPE)
                } ?: DEFAULT_CHAT_WALLPAPER_TYPE,
                value = preferences[KEY_CHAT_WALLPAPER_VALUE]
            )
        )
    }

    /**
     * Sets read receipts enabled state.
     */
    suspend fun setReadReceiptsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_READ_RECEIPTS_ENABLED] = enabled
        }
    }

    /**
     * Sets typing indicators enabled state.
     */
    suspend fun setTypingIndicatorsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_TYPING_INDICATORS_ENABLED] = enabled
        }
    }

    /**
     * Sets media auto-download preference.
     */
    suspend fun setMediaAutoDownload(setting: MediaAutoDownload) {
        dataStore.edit { preferences ->
            preferences[KEY_MEDIA_AUTO_DOWNLOAD] = setting.name
        }
    }

    /**
     * Sets chat font scale.
     */
    suspend fun setChatFontScale(scale: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_CHAT_FONT_SCALE] = scale
        }
    }

    /**
     * Sets chat theme preset.
     */
    suspend fun setChatThemePreset(preset: ChatThemePreset) {
        dataStore.edit { preferences ->
            preferences[KEY_CHAT_THEME_PRESET] = preset.name
        }
    }

    /**
     * Sets chat wallpaper.
     */
    suspend fun setChatWallpaper(wallpaper: ChatWallpaper) {
        dataStore.edit { preferences ->
            preferences[KEY_CHAT_WALLPAPER_TYPE] = wallpaper.type.name
            if (wallpaper.value != null) {
                preferences[KEY_CHAT_WALLPAPER_VALUE] = wallpaper.value
            } else {
                preferences.remove(KEY_CHAT_WALLPAPER_VALUE)
            }
        }
    }


    // ========================================================================
    // Storage and Data Settings
    // ========================================================================

    /**
     * Flow of media upload quality.
     */
    val mediaUploadQuality: Flow<com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaUploadQuality> = safePreferencesFlow().map { preferences ->
        preferences[KEY_MEDIA_UPLOAD_QUALITY]?.let { value ->
            runCatching { com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaUploadQuality.valueOf(value) }
                .getOrDefault(com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaUploadQuality.STANDARD)
        } ?: com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaUploadQuality.STANDARD
    }

    /**
     * Sets media upload quality.
     */
    suspend fun setMediaUploadQuality(quality: com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaUploadQuality) {
        dataStore.edit { preferences ->
            preferences[KEY_MEDIA_UPLOAD_QUALITY] = quality.name
        }
    }

    /**
     * Flow of use less data for calls setting.
     */
    val useLessDataCalls: Flow<Boolean> = safePreferencesFlow().map { preferences ->
        preferences[KEY_USE_LESS_DATA_CALLS] ?: false
    }

    /**
     * Sets use less data for calls.
     */
    suspend fun setUseLessDataCalls(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_USE_LESS_DATA_CALLS] = enabled
        }
    }

    /**
     * Flow of auto-download rules.
     */
    val autoDownloadRules: Flow<com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.AutoDownloadRules> = safePreferencesFlow().map { preferences ->
        com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.AutoDownloadRules(
            mobileData = preferences[KEY_AUTO_DOWNLOAD_MOBILE]?.mapNotNull {
                runCatching { com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaType.valueOf(it) }.getOrNull()
            }?.toSet() ?: setOf(com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaType.PHOTO),

            wifi = preferences[KEY_AUTO_DOWNLOAD_WIFI]?.mapNotNull {
                runCatching { com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaType.valueOf(it) }.getOrNull()
            }?.toSet() ?: com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaType.values().toSet(),

            roaming = preferences[KEY_AUTO_DOWNLOAD_ROAMING]?.mapNotNull {
                runCatching { com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaType.valueOf(it) }.getOrNull()
            }?.toSet() ?: emptySet()
        )
    }

    /**
     * Sets auto-download rules for a specific network type.
     */
    suspend fun setAutoDownloadRule(
        networkType: String, // "mobile", "wifi", "roaming"
        mediaTypes: Set<com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaType>
    ) {
        dataStore.edit { preferences ->
            val key = when (networkType) {
                "mobile" -> KEY_AUTO_DOWNLOAD_MOBILE
                "wifi" -> KEY_AUTO_DOWNLOAD_WIFI
                "roaming" -> KEY_AUTO_DOWNLOAD_ROAMING
                else -> return@edit
            }
            preferences[key] = mediaTypes.map { it.name }.toSet()
        }
    }

    // ========================================================================
    // Data Saver Settings
    // ========================================================================

    /**
     * Flow of data saver enabled state.
     */
    val dataSaverEnabled: Flow<Boolean> = safePreferencesFlow().map { preferences ->
        preferences[KEY_DATA_SAVER_ENABLED] ?: DEFAULT_DATA_SAVER_ENABLED
    }

    /**
     * Sets data saver enabled state.
     */
    suspend fun setDataSaverEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DATA_SAVER_ENABLED] = enabled
        }
    }

    /**
     * Sets enter is send enabled state.
     */
    suspend fun setEnterIsSendEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ENTER_IS_SEND_ENABLED] = enabled
        }
    }

    /**
     * Sets media visibility enabled state.
     */
    suspend fun setMediaVisibilityEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_MEDIA_VISIBILITY_ENABLED] = enabled
        }
    }

    /**
     * Sets voice transcripts enabled state.
     */
    suspend fun setVoiceTranscriptsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_VOICE_TRANSCRIPTS_ENABLED] = enabled
        }
    }

    /**
     * Sets auto backup enabled state.
     */
    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_BACKUP_ENABLED] = enabled
        }
    }

    /**
     * Sets reminders enabled state.
     */
    suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_REMINDERS_ENABLED] = enabled
        }
    }

    /**
     * Sets high priority enabled state.
     */
    suspend fun setHighPriorityEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HIGH_PRIORITY_ENABLED] = enabled
        }
    }

    /**
     * Sets reaction notifications enabled state.
     */
    suspend fun setReactionNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_REACTION_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    /**
     * Sets app lock enabled state.
     */
    suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_APP_LOCK_ENABLED] = enabled
        }
    }

    /**
     * Sets chat lock enabled state.
     */
    suspend fun setChatLockEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_CHAT_LOCK_ENABLED] = enabled
        }
    }

    // ========================================================================
    // Request Account Info Settings
    // ========================================================================

    /**
     * Flow of account reports auto create enabled state.
     */
    val accountReportsAutoCreate: Flow<Boolean> = safePreferencesFlow().map { preferences ->
        preferences[KEY_ACCOUNT_REPORTS_AUTO_CREATE] ?: DEFAULT_ACCOUNT_REPORTS_AUTO_CREATE
    }

    /**
     * Sets account reports auto create enabled state.
     */
    suspend fun setAccountReportsAutoCreate(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ACCOUNT_REPORTS_AUTO_CREATE] = enabled
        }
    }

    /**
     * Flow of channels reports auto create enabled state.
     */
    val channelsReportsAutoCreate: Flow<Boolean> = safePreferencesFlow().map { preferences ->
        preferences[KEY_CHANNELS_REPORTS_AUTO_CREATE] ?: DEFAULT_CHANNELS_REPORTS_AUTO_CREATE
    }

    /**
     * Sets channels reports auto create enabled state.
     */
    suspend fun setChannelsReportsAutoCreate(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_CHANNELS_REPORTS_AUTO_CREATE] = enabled
        }
    }

    // ========================================================================
    // Settings Lifecycle Management
    // ========================================================================

    /**
     * Clears user-specific settings while preserving device-level preferences.
     *
     * Device-level settings preserved:
     * - Theme mode
     * - Dynamic color
     * - Font scale
     *
     * User-level settings cleared:
     * - Privacy settings
     * - Notification preferences
     * - Chat settings
     * - Data saver
     *
     * Requirements: 10.3
     */
    suspend fun clearUserSettings() {
        dataStore.edit { preferences ->
            // Remove user-level settings only
            // Privacy
            preferences.remove(KEY_PROFILE_VISIBILITY)
            preferences.remove(KEY_CONTENT_VISIBILITY)
            preferences.remove(KEY_BIOMETRIC_LOCK_ENABLED)
            preferences.remove(KEY_TWO_FACTOR_ENABLED)

            // Notifications
            preferences.remove(KEY_NOTIFICATIONS_LIKES)
            preferences.remove(KEY_NOTIFICATIONS_COMMENTS)
            preferences.remove(KEY_NOTIFICATIONS_FOLLOWS)
            preferences.remove(KEY_NOTIFICATIONS_MESSAGES)
            preferences.remove(KEY_NOTIFICATIONS_MENTIONS)
            preferences.remove(KEY_IN_APP_NOTIFICATIONS)

            // Chat
            preferences.remove(KEY_READ_RECEIPTS_ENABLED)
            preferences.remove(KEY_TYPING_INDICATORS_ENABLED)
            preferences.remove(KEY_MEDIA_AUTO_DOWNLOAD)
            preferences.remove<Float>(KEY_CHAT_FONT_SCALE)
            preferences.remove(KEY_CHAT_THEME_PRESET)
            preferences.remove(KEY_CHAT_WALLPAPER_TYPE)
            preferences.remove(KEY_CHAT_WALLPAPER_VALUE)

            // Data saver
            preferences.remove(KEY_DATA_SAVER_ENABLED)

            // Storage and Data
            preferences.remove(KEY_MEDIA_UPLOAD_QUALITY)
            preferences.remove(KEY_USE_LESS_DATA_CALLS)
            preferences.remove(KEY_AUTO_DOWNLOAD_MOBILE)
            preferences.remove(KEY_AUTO_DOWNLOAD_WIFI)
            preferences.remove(KEY_AUTO_DOWNLOAD_ROAMING)

            // Request Account Info
            preferences.remove(KEY_ACCOUNT_REPORTS_AUTO_CREATE)
            preferences.remove(KEY_CHANNELS_REPORTS_AUTO_CREATE)
        }
    }

    /**
     * Clears all settings including device-level preferences.
     */
    suspend fun clearAllSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Restores all settings to their default values.
     */
    suspend fun restoreDefaults() {
        dataStore.edit { preferences ->
            // Device-level
            preferences[KEY_THEME_MODE] = DEFAULT_THEME_MODE.name
            preferences[KEY_DYNAMIC_COLOR_ENABLED] = DEFAULT_DYNAMIC_COLOR_ENABLED
            preferences[KEY_FONT_SCALE] = DEFAULT_FONT_SCALE.name
            preferences[KEY_APP_LANGUAGE] = DEFAULT_APP_LANGUAGE

            // Privacy
            preferences[KEY_PROFILE_VISIBILITY] = DEFAULT_PROFILE_VISIBILITY.name
            preferences[KEY_CONTENT_VISIBILITY] = DEFAULT_CONTENT_VISIBILITY.name
            preferences[KEY_BIOMETRIC_LOCK_ENABLED] = DEFAULT_BIOMETRIC_LOCK_ENABLED
            preferences[KEY_TWO_FACTOR_ENABLED] = DEFAULT_TWO_FACTOR_ENABLED

            // Notifications
            preferences[KEY_NOTIFICATIONS_LIKES] = DEFAULT_NOTIFICATIONS_ENABLED
            preferences[KEY_NOTIFICATIONS_COMMENTS] = DEFAULT_NOTIFICATIONS_ENABLED
            preferences[KEY_NOTIFICATIONS_FOLLOWS] = DEFAULT_NOTIFICATIONS_ENABLED
            preferences[KEY_NOTIFICATIONS_MESSAGES] = DEFAULT_NOTIFICATIONS_ENABLED
            preferences[KEY_NOTIFICATIONS_MENTIONS] = DEFAULT_NOTIFICATIONS_ENABLED
            preferences[KEY_IN_APP_NOTIFICATIONS] = DEFAULT_IN_APP_NOTIFICATIONS_ENABLED

            // Chat
            preferences[KEY_READ_RECEIPTS_ENABLED] = DEFAULT_READ_RECEIPTS_ENABLED
            preferences[KEY_TYPING_INDICATORS_ENABLED] = DEFAULT_TYPING_INDICATORS_ENABLED
            preferences[KEY_MEDIA_AUTO_DOWNLOAD] = DEFAULT_MEDIA_AUTO_DOWNLOAD.name
            preferences[KEY_CHAT_FONT_SCALE] = DEFAULT_CHAT_FONT_SCALE
            preferences[KEY_CHAT_THEME_PRESET] = DEFAULT_CHAT_THEME_PRESET.name
            preferences[KEY_CHAT_WALLPAPER_TYPE] = DEFAULT_CHAT_WALLPAPER_TYPE.name
            preferences.remove(KEY_CHAT_WALLPAPER_VALUE)

            // Data saver
            preferences[KEY_DATA_SAVER_ENABLED] = DEFAULT_DATA_SAVER_ENABLED

            // Storage and Data
            preferences[KEY_MEDIA_UPLOAD_QUALITY] = com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaUploadQuality.STANDARD.name
            preferences[KEY_USE_LESS_DATA_CALLS] = false
            preferences[KEY_AUTO_DOWNLOAD_MOBILE] = setOf(com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaType.PHOTO.name)
            preferences[KEY_AUTO_DOWNLOAD_WIFI] = com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings.MediaType.values().map { it.name }.toSet()
            preferences[KEY_AUTO_DOWNLOAD_ROAMING] = emptySet()

            // Request Account Info
            preferences[KEY_ACCOUNT_REPORTS_AUTO_CREATE] = DEFAULT_ACCOUNT_REPORTS_AUTO_CREATE
            preferences[KEY_CHANNELS_REPORTS_AUTO_CREATE] = DEFAULT_CHANNELS_REPORTS_AUTO_CREATE
        }
    }
}
