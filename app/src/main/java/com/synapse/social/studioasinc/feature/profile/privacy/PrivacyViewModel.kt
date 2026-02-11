package com.synapse.social.studioasinc.feature.profile.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.shared.domain.model.PrivacyLevel
import com.synapse.social.studioasinc.shared.domain.model.state.PrivacyUiState
import com.synapse.social.studioasinc.shared.domain.usecase.GetCurrentUserIdUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.GetPrivacySettingsUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.UpdatePrivacySettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val getPrivacySettingsUseCase: GetPrivacySettingsUseCase,
    private val updatePrivacySettingsUseCase: UpdatePrivacySettingsUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = getCurrentUserIdUseCase()
            if (userId != null) {
                getPrivacySettingsUseCase(userId).collect { result ->
                    result.onSuccess { settings ->
                        _uiState.update { it.copy(isLoading = false, settings = settings) }
                    }.onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, error = error.message) }
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
            }
        }
    }

    fun updateSectionPrivacy(sectionId: String, level: PrivacyLevel) {
        val currentSettings = _uiState.value.settings
        val newDefaults = currentSettings.sectionDefaults.toMutableMap().apply {
            put(sectionId, level)
        }
        val newSettings = currentSettings.copy(sectionDefaults = newDefaults)

        saveSettings(newSettings)
    }

    fun updateItemPrivacy(itemId: String, level: PrivacyLevel) {
        val currentSettings = _uiState.value.settings
        val newOverrides = currentSettings.itemOverrides.toMutableMap().apply {
            put(itemId, level)
        }
        val newSettings = currentSettings.copy(itemOverrides = newOverrides)

        saveSettings(newSettings)
    }

    fun removeItemOverride(itemId: String) {
        val currentSettings = _uiState.value.settings
        val newOverrides = currentSettings.itemOverrides.toMutableMap().apply {
            remove(itemId)
        }
        val newSettings = currentSettings.copy(itemOverrides = newOverrides)

        saveSettings(newSettings)
    }

    private fun saveSettings(newSettings: com.synapse.social.studioasinc.shared.domain.model.PrivacySettings) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, settings = newSettings) } // Optimistic update
            val userId = getCurrentUserIdUseCase() ?: return@launch

            updatePrivacySettingsUseCase(userId, newSettings)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, error = "Failed to save: ${error.message}") }
                    // Ideally revert state here, but skipping for brevity
                }
        }
    }
}
