package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.local.database.SettingsDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Request Status for the report generation process.
 */
sealed class RequestStatus {
    object Idle : RequestStatus()
    object Processing : RequestStatus()
    data class Ready(val availableUntil: String) : RequestStatus()
}

/**
 * UI State for Request Account Info screen.
 */
data class RequestAccountInfoUiState(
    val isAccountInfoSelected: Boolean = false,
    val isChannelActivitySelected: Boolean = false,
    val isAutoReportEnabled: Boolean = false,
    val status: RequestStatus = RequestStatus.Idle,
    val error: String? = null
)

/**
 * ViewModel for Request Account Info screen.
 *
 * Manages state for requesting account reports and toggling auto-creation.
 * Simulates API calls for report generation.
 */
class RequestAccountInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore.getInstance(application)

    // ========================================================================
    // State
    // ========================================================================

    private val _uiState = MutableStateFlow(RequestAccountInfoUiState())
    val uiState: StateFlow<RequestAccountInfoUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Loads initial settings from DataStore.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            val accountAuto = settingsDataStore.accountReportsAutoCreate.first()
            val channelsAuto = settingsDataStore.channelsReportsAutoCreate.first()

            // If either is enabled, we show the master toggle as enabled
            _uiState.value = _uiState.value.copy(
                isAutoReportEnabled = accountAuto || channelsAuto
            )
        }
    }

    fun toggleAccountSelection() {
        if (_uiState.value.status !is RequestStatus.Processing) {
            _uiState.value = _uiState.value.copy(
                isAccountInfoSelected = !_uiState.value.isAccountInfoSelected
            )
        }
    }

    fun toggleChannelSelection() {
        if (_uiState.value.status !is RequestStatus.Processing) {
            _uiState.value = _uiState.value.copy(
                isChannelActivitySelected = !_uiState.value.isChannelActivitySelected
            )
        }
    }

    fun toggleAutoReport(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAutoReportEnabled = enabled)
            // Update both preferences to match the single simplified toggle
            settingsDataStore.setAccountReportsAutoCreate(enabled)
            settingsDataStore.setChannelsReportsAutoCreate(enabled)
        }
    }

    fun requestReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = RequestStatus.Processing)

            try {
                // Simulate network delay
                delay(3000)

                // Calculate ready date (3 days from now)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, 3)
                val readyDate = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(calendar.time)

                _uiState.value = _uiState.value.copy(
                    status = RequestStatus.Ready(readyDate)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    status = RequestStatus.Idle,
                    error = "Failed to request report. Please try again."
                )
            }
        }
    }

    /**
     * Clears error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
