package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.data.model.AppUpdateInfo
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getAppearanceSettings(): Flow<AppearanceSettings>
    suspend fun updateThemeMode(mode: ThemeMode)
    suspend fun updateFontScale(scale: FontScale)
    suspend fun getPrivacySettings(): Flow<PrivacySettings>
    suspend fun updateProfileVisibility(visibility: ProfileVisibility)
    suspend fun updateContentVisibility(visibility: ContentVisibility)
    suspend fun updateGroupPrivacy(privacy: GroupPrivacy)
    fun getNotificationPreferences(): Flow<NotificationPreferences>
    suspend fun updateNotificationCategory(category: NotificationCategory, enabled: Boolean)
    fun getChatSettings(): Flow<ChatSettings>
    suspend fun updateMediaAutoDownload(mode: MediaAutoDownload)
    suspend fun checkAppUpdate(): Result<AppUpdateInfo?>
}
