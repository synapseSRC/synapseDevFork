package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.repository.SettingsRepository
import com.russhwolf.multiplatform.settings.Settings
import com.russhwolf.multiplatform.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.aakira.napier.Napier

class SettingsRepositoryImpl(private val settings: Settings) : SettingsRepository {

    private val flowSettings = settings.toFlowSettings()

    override fun getTheme(): Flow<String> = flowSettings.getStringFlow("theme", "system")
    override suspend fun setTheme(theme: String) = settings.putString("theme", theme)

    override fun getLanguage(): Flow<String> = flowSettings.getStringFlow("language", "en")
    override suspend fun setLanguage(language: String) = settings.putString("language", language)

    override fun isNotificationsEnabled(): Flow<Boolean> = flowSettings.getBooleanFlow("notifications_enabled", true)
    override suspend fun setNotificationsEnabled(enabled: Boolean) = settings.putBoolean("notifications_enabled", enabled)

    override fun isBiometricsEnabled(): Flow<Boolean> = flowSettings.getBooleanFlow("biometrics_enabled", false)
    override suspend fun setBiometricsEnabled(enabled: Boolean) = settings.putBoolean("biometrics_enabled", enabled)

    override fun isUseLessDataCalls(): Flow<Boolean> = flowSettings.getBooleanFlow("use_less_data_calls", false)
    override suspend fun setUseLessDataCalls(enabled: Boolean) = settings.putBoolean("use_less_data_calls", enabled)

    override val autoDownloadRules: Flow<AutoDownloadRules> = flowSettings.getStringFlow("auto_download_rules", "").map {
        AutoDownloadRules() // Simplified for now
    }

    override suspend fun setAutoDownloadRule(networkType: String, mediaTypes: Set<MediaType>) {
        // Logic to save rule
    }

    override suspend fun checkForUpdates(): Result<AppUpdateInfo?> = Result.success(null)

    override suspend fun setDataSaverEnabled(enabled: Boolean) = settings.putBoolean("data_saver_enabled", enabled)
    override suspend fun setEnterIsSendEnabled(enabled: Boolean) = settings.putBoolean("enter_is_send_enabled", enabled)
    override suspend fun setMediaVisibilityEnabled(enabled: Boolean) = settings.putBoolean("media_visibility_enabled", enabled)
    override suspend fun setVoiceTranscriptsEnabled(enabled: Boolean) = settings.putBoolean("voice_transcripts_enabled", enabled)
    override suspend fun setAutoBackupEnabled(enabled: Boolean) = settings.putBoolean("auto_backup_enabled", enabled)
    override suspend fun setRemindersEnabled(enabled: Boolean) = settings.putBoolean("reminders_enabled", enabled)
    override suspend fun setHighPriorityEnabled(enabled: Boolean) = settings.putBoolean("high_priority_enabled", enabled)
    override suspend fun setReactionNotificationsEnabled(enabled: Boolean) = settings.putBoolean("reaction_notifications_enabled", enabled)
    override suspend fun setAppLockEnabled(enabled: Boolean) = settings.putBoolean("app_lock_enabled", enabled)
    override suspend fun setChatLockEnabled(enabled: Boolean) = settings.putBoolean("chat_lock_enabled", enabled)

    override suspend fun clearUserSettings() {
        // Clear logic
    }

    override suspend fun clearAllSettings() {
        settings.clear()
    }

    override suspend fun restoreDefaults() {
        settings.clear()
    }
}
