package com.synapse.social.studioasinc.core

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.synapse.social.studioasinc.data.remote.services.SupabaseAuthenticationService
import com.synapse.social.studioasinc.data.remote.services.AuthDevelopmentUtils
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.synapse.social.studioasinc.core.config.NotificationConfig
import com.synapse.social.studioasinc.core.util.MediaCacheCleanupManager
import com.synapse.social.studioasinc.data.repository.SettingsRepositoryImpl
import com.synapse.social.studioasinc.ui.theme.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class SynapseApplication : Application() {

    private lateinit var mediaCacheCleanupManager: MediaCacheCleanupManager

    override fun onCreate() {
        super.onCreate()

        // Initialize OneSignal
        initializeOneSignal()

        // Initialize authentication service
        SupabaseAuthenticationService.initialize(this)

        // Initialize background maintenance services
        initializeMaintenanceServices()

        // Apply saved theme on app startup
        applyThemeOnStartup()

        // Log authentication configuration in development builds
        if (AuthDevelopmentUtils.isDevelopmentBuild()) {
            AuthDevelopmentUtils.logAuthConfig(this)
        }
    }

    private fun initializeOneSignal() {
        // Enable debug logging only in debug builds and if configured
        if (com.synapse.social.studioasinc.BuildConfig.DEBUG && NotificationConfig.ENABLE_DEBUG_LOGGING) {
            OneSignal.Debug.logLevel = LogLevel.VERBOSE
        }

        // Initialize OneSignal
        OneSignal.initWithContext(this, NotificationConfig.ONESIGNAL_APP_ID)

        // Prompt for notification permission on Android 13+
        // Note: The SDK v5 recommendation is to use an In-App Message,
        // but we'll use the direct prompt for now to ensure delivery works.
        CoroutineScope(Dispatchers.Main).launch {
            OneSignal.Notifications.requestPermission(true)

            // Link existing session to OneSignal if available
            try {
                val authService = SupabaseAuthenticationService.getInstance(this@SynapseApplication)
                authService.getCurrentUserId()?.let { userId ->
                    OneSignal.login(userId)
                    android.util.Log.d("SynapseApplication", "Restored OneSignal session for user: $userId")
                }
            } catch (e: Exception) {
                android.util.Log.e("SynapseApplication", "Failed to restore OneSignal session", e)
            }
        }
    }

    private fun applyThemeOnStartup() {
        val settingsRepository = SettingsRepositoryImpl.getInstance(this)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                settingsRepository.appearanceSettings.collect { settings ->
                    ThemeManager.applyThemeMode(settings.themeMode)
                }
            } catch (e: Exception) {
                android.util.Log.e("SynapseApplication", "Failed to apply theme on startup", e)
            }
        }
    }

    private fun initializeMaintenanceServices() {
        // Initialize media cache cleanup
        mediaCacheCleanupManager = MediaCacheCleanupManager(this)
        mediaCacheCleanupManager.initialize()
    }

    override fun onTerminate() {
        super.onTerminate()

        // Clean up maintenance services
        if (::mediaCacheCleanupManager.isInitialized) {
            mediaCacheCleanupManager.shutdown()
        }
    }
}
