package com.synapse.social.studioasinc.ui.settings

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the About and Support Settings screen.
 *
 * Manages the state for app information, version details, and support-related
 * functionality including external link navigation and feedback submission.
 *
 * Requirements: 9.1, 9.5
 */
class AboutSupportViewModel(
    application: Application
) : AndroidViewModel(application) {

    // ========================================================================
    // State
    // ========================================================================

    private val _appVersion = MutableStateFlow("")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    private val _buildNumber = MutableStateFlow("")
    val buildNumber: StateFlow<String> = _buildNumber.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadAppInfo()
    }

    // ========================================================================
    // App Information
    // ========================================================================

    /**
     * Loads app version and build information from PackageManager.
     *
     * Requirements: 9.1
     */
    private fun loadAppInfo() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    0
                )

                _appVersion.value = packageInfo.versionName ?: "Unknown"

                // Get version code based on API level
                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toString()
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toString()
                }

                _buildNumber.value = versionCode

                android.util.Log.d(
                    "AboutSupportViewModel",
                    "App version: ${_appVersion.value}, Build: ${_buildNumber.value}"
                )
            } catch (e: PackageManager.NameNotFoundException) {
                android.util.Log.e("AboutSupportViewModel", "Failed to load app info", e)
                _appVersion.value = "Unknown"
                _buildNumber.value = "Unknown"
            }
        }
    }

    // ========================================================================
    // External Link Navigation
    // ========================================================================

    /**
     * Handles navigation to Terms of Service.
     * Returns the URL to be opened in a browser.
     *
     * Requirements: 9.2
     */
    fun getTermsOfServiceUrl(): String {
        return "https://synapsesocial.vercel.app/reference/terms/"
    }

    /**
     * Handles navigation to Privacy Policy.
     * Returns the URL to be opened in a browser.
     *
     * Requirements: 9.3
     */
    fun getPrivacyPolicyUrl(): String {
        return "https://synapsesocial.vercel.app/reference/privacy/"
    }

    /**
     * Handles navigation to Help Center.
     * Returns the URL to be opened in a browser.
     *
     * Requirements: 9.4
     */
    fun navigateToHelpCenter() {
        android.util.Log.d("AboutSupportViewModel", "Navigate to Help Center")
        // Navigation will be handled by the screen composable
    }

    /**
     * Gets the Help Center URL.
     * Returns the URL to be opened in a browser.
     *
     * Requirements: 9.4
     */
    fun getHelpCenterUrl(): String {
        return "https://synapsesocial.vercel.app/"
    }

    /**
     * Handles navigation to Open Source Licenses screen.
     * This is a placeholder for future implementation.
     *
     * Requirements: 9.7
     */
    fun navigateToLicenses() {
        android.util.Log.d("AboutSupportViewModel", "Navigate to Open Source Licenses (placeholder)")
        // Navigation will be handled by the screen composable
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    /**
     * Clears any error messages.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clears any success messages.
     */
    fun clearMessage() {
        _message.value = null
    }

    /**
     * Gets the full app version string including build number.
     *
     * @return Formatted version string (e.g., "1.0.0 (15)")
     */
    fun getFullVersionString(): String {
        return "${_appVersion.value} (${_buildNumber.value})"
    }

}
