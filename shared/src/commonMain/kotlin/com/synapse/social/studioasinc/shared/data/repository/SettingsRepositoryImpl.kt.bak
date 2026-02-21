package com.synapse.social.studioasinc.shared.data.repository

import android.content.Context
import io.github.aakira.napier.Napier
import com.synapse.social.studioasinc.data.local.database.SettingsDataStore
import com.synapse.social.studioasinc.ui.settings.AppearanceSettings
import com.synapse.social.studioasinc.ui.settings.ChatSettings
import com.synapse.social.studioasinc.ui.settings.ContentVisibility
import com.synapse.social.studioasinc.ui.settings.FontScale
import com.synapse.social.studioasinc.ui.settings.GroupPrivacy
import com.synapse.social.studioasinc.ui.settings.MediaAutoDownload
import com.synapse.social.studioasinc.ui.settings.NotificationCategory
import com.synapse.social.studioasinc.ui.settings.NotificationPreferences
import com.synapse.social.studioasinc.ui.settings.PrivacySettings
import com.synapse.social.studioasinc.ui.settings.ProfileVisibility
import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.data.model.AppUpdateInfo
import com.synapse.social.studioasinc.ui.settings.ThemeMode
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File



class SettingsRepositoryImpl private constructor(
    private val context: Context,
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    companion object {
        private const val TAG = "SettingsRepositoryImpl"

        @Volatile
        private var INSTANCE: SettingsRepositoryImpl? = null

        fun getInstance(context: Context): SettingsRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                val dataStore = SettingsDataStore.getInstance(context)
                INSTANCE ?: SettingsRepositoryImpl(
                    context.applicationContext,
                    dataStore
                ).also { INSTANCE = it }
            }
        }
    }



    private val _cacheSize = MutableStateFlow(0L)





    override val themeMode: Flow<ThemeMode> = settingsDataStore.themeMode

    override val dynamicColorEnabled: Flow<Boolean> = settingsDataStore.dynamicColorEnabled

    override val fontScale: Flow<FontScale> = settingsDataStore.fontScale

    override val appearanceSettings: Flow<AppearanceSettings> = settingsDataStore.appearanceSettings

    override suspend fun setThemeMode(mode: ThemeMode) {
        settingsDataStore.setThemeMode(mode)
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        settingsDataStore.setDynamicColorEnabled(enabled)
    }

    override suspend fun setFontScale(scale: FontScale) {
        settingsDataStore.setFontScale(scale)
    }

    override suspend fun setPostViewStyle(style: com.synapse.social.studioasinc.ui.settings.PostViewStyle) {
        settingsDataStore.setPostViewStyle(style)
    }

    override val language: Flow<String> = settingsDataStore.language

    override suspend fun setLanguage(languageCode: String) {
        settingsDataStore.setLanguage(languageCode)
    }





    override val profileVisibility: Flow<ProfileVisibility> = settingsDataStore.profileVisibility

    override val contentVisibility: Flow<ContentVisibility> = settingsDataStore.contentVisibility

    override val biometricLockEnabled: Flow<Boolean> = settingsDataStore.biometricLockEnabled

    override val twoFactorEnabled: Flow<Boolean> = settingsDataStore.twoFactorEnabled

    override val privacySettings: Flow<PrivacySettings> = settingsDataStore.privacySettings

    override suspend fun setProfileVisibility(visibility: ProfileVisibility) {
        settingsDataStore.setProfileVisibility(visibility)
    }

    override suspend fun setContentVisibility(visibility: ContentVisibility) {
        settingsDataStore.setContentVisibility(visibility)
    }

    override suspend fun setGroupPrivacy(privacy: GroupPrivacy) {
        settingsDataStore.setGroupPrivacy(privacy)
    }

    override suspend fun setBiometricLockEnabled(enabled: Boolean) {
        settingsDataStore.setBiometricLockEnabled(enabled)
    }

    override suspend fun setTwoFactorEnabled(enabled: Boolean) {
        settingsDataStore.setTwoFactorEnabled(enabled)
    }





    override val notificationPreferences: Flow<NotificationPreferences> =
        settingsDataStore.notificationPreferences

    override suspend fun updateNotificationPreference(category: NotificationCategory, enabled: Boolean) {
        settingsDataStore.updateNotificationPreference(category, enabled)
    }

    override suspend fun setInAppNotificationsEnabled(enabled: Boolean) {
        settingsDataStore.setInAppNotificationsEnabled(enabled)
    }





    override val chatSettings: Flow<ChatSettings> = settingsDataStore.chatSettings

    override suspend fun setReadReceiptsEnabled(enabled: Boolean) {
        settingsDataStore.setReadReceiptsEnabled(enabled)
    }

    override suspend fun setTypingIndicatorsEnabled(enabled: Boolean) {
        settingsDataStore.setTypingIndicatorsEnabled(enabled)
    }

    override suspend fun setMediaAutoDownload(setting: MediaAutoDownload) {
        settingsDataStore.setMediaAutoDownload(setting)
    }

    override suspend fun setChatFontScale(scale: Float) {
        settingsDataStore.setChatFontScale(scale)
    }

    override suspend fun setChatThemePreset(preset: com.synapse.social.studioasinc.shared.domain.model.ChatThemePreset) {
        settingsDataStore.setChatThemePreset(preset)
    }

    override suspend fun setChatWallpaper(wallpaper: com.synapse.social.studioasinc.shared.domain.model.ChatWallpaper) {
        settingsDataStore.setChatWallpaper(wallpaper)
    }







    override val mediaUploadQuality: Flow<com.synapse.social.studioasinc.ui.settings.MediaUploadQuality> = settingsDataStore.mediaUploadQuality

    override suspend fun setMediaUploadQuality(quality: com.synapse.social.studioasinc.ui.settings.MediaUploadQuality) {
        settingsDataStore.setMediaUploadQuality(quality)
    }

    override val useLessDataCalls: Flow<Boolean> = settingsDataStore.useLessDataCalls

    override suspend fun setUseLessDataCalls(enabled: Boolean) {
        settingsDataStore.setUseLessDataCalls(enabled)
    }

    override val autoDownloadRules: Flow<com.synapse.social.studioasinc.ui.settings.AutoDownloadRules> = settingsDataStore.autoDownloadRules

    override suspend fun setAutoDownloadRule(
        networkType: String,
        mediaTypes: Set<com.synapse.social.studioasinc.ui.settings.MediaType>
    ) {
        settingsDataStore.setAutoDownloadRule(networkType, mediaTypes)
    }

    override val cacheSize: Flow<Long> = _cacheSize.asStateFlow()



    override suspend fun clearCache(): Long {
        val sizeBefore = calculateCacheSize()

        try {

            context.cacheDir?.let { cacheDir ->
                deleteDirectory(cacheDir)
            }


            context.externalCacheDir?.let { externalCacheDir ->
                deleteDirectory(externalCacheDir)
            }


            context.codeCacheDir?.let { codeCacheDir ->
                deleteDirectory(codeCacheDir)
            }
        } catch (e: Exception) {
            Napier.e(TAG, "Error clearing cache", e)
        }

        val sizeAfter = calculateCacheSize()
        val freedSpace = sizeBefore - sizeAfter


        _cacheSize.value = sizeAfter

        Napier.d(TAG, "Cache cleared: freed ${freedSpace / 1024}KB")
        return freedSpace
    }



    override suspend fun calculateCacheSize(): Long {
        var totalSize = 0L

        try {

            context.cacheDir?.let { cacheDir ->
                totalSize += getDirectorySize(cacheDir)
            }


            context.externalCacheDir?.let { externalCacheDir ->
                totalSize += getDirectorySize(externalCacheDir)
            }


            context.codeCacheDir?.let { codeCacheDir ->
                totalSize += getDirectorySize(codeCacheDir)
            }
        } catch (e: Exception) {
            Napier.e(TAG, "Error calculating cache size", e)
        }


        _cacheSize.value = totalSize

        return totalSize
    }



    private fun getDirectorySize(directory: File): Long {
        var size = 0L

        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    file.length()
                }
            }
        }

        return size
    }



    private fun deleteDirectory(directory: File): Boolean {
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    deleteDirectory(file)
                }
                file.delete()
            }
        }
        return true
    }






    override val dataSaverEnabled: Flow<Boolean> = settingsDataStore.dataSaverEnabled

    override suspend fun setDataSaverEnabled(enabled: Boolean) {
        settingsDataStore.setDataSaverEnabled(enabled)
    }

    override suspend fun setEnterIsSendEnabled(enabled: Boolean) {
        settingsDataStore.setEnterIsSendEnabled(enabled)
    }

    override suspend fun setMediaVisibilityEnabled(enabled: Boolean) {
        settingsDataStore.setMediaVisibilityEnabled(enabled)
    }

    override suspend fun setVoiceTranscriptsEnabled(enabled: Boolean) {
        settingsDataStore.setVoiceTranscriptsEnabled(enabled)
    }

    override suspend fun setAutoBackupEnabled(enabled: Boolean) {
        settingsDataStore.setAutoBackupEnabled(enabled)
    }

    override suspend fun setRemindersEnabled(enabled: Boolean) {
        settingsDataStore.setRemindersEnabled(enabled)
    }

    override suspend fun setHighPriorityEnabled(enabled: Boolean) {
        settingsDataStore.setHighPriorityEnabled(enabled)
    }

    override suspend fun setReactionNotificationsEnabled(enabled: Boolean) {
        settingsDataStore.setReactionNotificationsEnabled(enabled)
    }

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        settingsDataStore.setAppLockEnabled(enabled)
    }

    override suspend fun setChatLockEnabled(enabled: Boolean) {
        settingsDataStore.setChatLockEnabled(enabled)
    }








    override suspend fun clearUserSettings() {
        settingsDataStore.clearUserSettings()
    }



    override suspend fun clearAllSettings() {
        settingsDataStore.clearAllSettings()
    }



    override suspend fun restoreDefaults() {
        settingsDataStore.restoreDefaults()
    }

    override suspend fun checkForUpdates(): Result<AppUpdateInfo?> {
        return try {
            val latestVersion = SupabaseClient.client
                .from("app_versions")
                .select() {
                    order("version_code", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<AppUpdateInfo>()

            Result.success(latestVersion)
        } catch (e: Exception) {
            Napier.e(TAG, "Failed to check for updates", e)
            Result.failure(e)
        }
    }
}
