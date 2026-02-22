package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.*
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getTheme(): Flow<String>
    suspend fun setTheme(theme: String)
    fun getLanguage(): Flow<String>
    suspend fun setLanguage(language: String)
    fun isNotificationsEnabled(): Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)
    fun isBiometricsEnabled(): Flow<Boolean>
    suspend fun setBiometricsEnabled(enabled: Boolean)
    fun isUseLessDataCalls(): Flow<Boolean>
    suspend fun setUseLessDataCalls(enabled: Boolean)
    val autoDownloadRules: Flow<AutoDownloadRules>
    suspend fun setAutoDownloadRule(networkType: String, mediaTypes: Set<MediaType>)
    suspend fun checkForUpdates(): Result<AppUpdateInfo?>
    suspend fun setDataSaverEnabled(enabled: Boolean)
    suspend fun setEnterIsSendEnabled(enabled: Boolean)
    suspend fun setMediaVisibilityEnabled(enabled: Boolean)
    suspend fun setVoiceTranscriptsEnabled(enabled: Boolean)
    suspend fun setAutoBackupEnabled(enabled: Boolean)
    suspend fun setRemindersEnabled(enabled: Boolean)
    suspend fun setHighPriorityEnabled(enabled: Boolean)
    suspend fun setReactionNotificationsEnabled(enabled: Boolean)
    suspend fun setAppLockEnabled(enabled: Boolean)
    suspend fun setChatLockEnabled(enabled: Boolean)
    suspend fun clearUserSettings()
    suspend fun clearAllSettings()
    suspend fun restoreDefaults()
}
