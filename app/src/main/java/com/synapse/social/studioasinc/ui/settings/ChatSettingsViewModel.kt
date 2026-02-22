package com.synapse.social.studioasinc.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.shared.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class ChatSettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {





    private val _chatSettings = MutableStateFlow(ChatSettings())
    val chatSettings: StateFlow<ChatSettings> = _chatSettings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadChatSettings()
    }







    private fun loadChatSettings() {
        viewModelScope.launch {
            try {
                settingsRepository.chatSettings.collect { settings ->
                    _chatSettings.value = settings
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatSettingsViewModel", "Failed to load chat settings", e)
                _error.value = "Failed to load chat settings"
            }
        }
    }







    fun toggleReadReceipts(enabled: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                settingsRepository.setReadReceiptsEnabled(enabled)
                android.util.Log.d(
                    "ChatSettingsViewModel",
                    "Read receipts ${if (enabled) "enabled" else "disabled"}"
                )
            } catch (e: Exception) {
                android.util.Log.e("ChatSettingsViewModel", "Failed to toggle read receipts", e)
                _error.value = "Failed to update read receipts"
            } finally {
                _isLoading.value = false
            }
        }
    }







    fun toggleTypingIndicators(enabled: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                settingsRepository.setTypingIndicatorsEnabled(enabled)
                android.util.Log.d(
                    "ChatSettingsViewModel",
                    "Typing indicators ${if (enabled) "enabled" else "disabled"}"
                )
            } catch (e: Exception) {
                android.util.Log.e("ChatSettingsViewModel", "Failed to toggle typing indicators", e)
                _error.value = "Failed to update typing indicators"
            } finally {
                _isLoading.value = false
            }
        }
    }







    fun setMediaAutoDownload(setting: MediaAutoDownload) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                settingsRepository.setMediaAutoDownload(setting)
                android.util.Log.d(
                    "ChatSettingsViewModel",
                    "Media auto-download set to ${setting.displayName()}"
                )
            } catch (e: Exception) {
                android.util.Log.e("ChatSettingsViewModel", "Failed to set media auto-download", e)
                _error.value = "Failed to update media auto-download"
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun getMediaAutoDownloadOptions(): List<MediaAutoDownload> {
        return MediaAutoDownload.values().toList()
    }







    fun navigateToMessageRequests() {
        android.util.Log.d("ChatSettingsViewModel", "Navigate to message requests")

    }



    fun navigateToChatPrivacy() {
        android.util.Log.d("ChatSettingsViewModel", "Navigate to chat privacy")

    }



    fun navigateToChatCustomization() {
        android.util.Log.d("ChatSettingsViewModel", "Navigate to chat customization")

    }



    fun navigateToChatWallpapers() {
        android.util.Log.d("ChatSettingsViewModel", "Navigate to chat wallpapers")

    }



    fun navigateToChatHistoryDeletion() {
        android.util.Log.d("ChatSettingsViewModel", "Navigate to chat history deletion")

    }







    fun setChatFontScale(scale: Float) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                settingsRepository.setChatFontScale(scale)
                android.util.Log.d("ChatSettingsViewModel", "Chat font scale set to $scale")
            } catch (e: Exception) {
                android.util.Log.e("ChatSettingsViewModel", "Failed to set chat font scale", e)
                _error.value = "Failed to update chat font size"
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun getChatFontSizeSliderValue(scale: Float): Float {
        return when {
            scale <= 0.8f -> 0f
            scale <= 1.0f -> 1f
            scale <= 1.2f -> 2f
            else -> 3f
        }
    }



    fun getChatFontScaleFromSliderValue(value: Float): Float {
        return when (value.toInt()) {
            0 -> 0.8f
            1 -> 1.0f
            2 -> 1.2f
            3 -> 1.4f
            else -> 1.0f
        }
    }



    fun getChatFontScalePreviewText(scale: Float): String {
        return when {
            scale <= 0.8f -> "Small"
            scale <= 1.0f -> "Default"
            scale <= 1.2f -> "Large"
            else -> "Extra Large"
        }
    }







    fun setEnterIsSend(enabled: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                settingsRepository.setEnterIsSendEnabled(enabled)
                android.util.Log.d("ChatSettingsViewModel", "Enter is send ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                android.util.Log.e("ChatSettingsViewModel", "Failed to toggle enter is send", e)
                _error.value = "Failed to update enter is send"
            } finally {
                _isLoading.value = false
            }
        }
    }







    fun setMediaVisibility(enabled: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                settingsRepository.setMediaVisibilityEnabled(enabled)
                android.util.Log.d("ChatSettingsViewModel", "Media visibility ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                android.util.Log.e("ChatSettingsViewModel", "Failed to toggle media visibility", e)
                _error.value = "Failed to update media visibility"
            } finally {
                _isLoading.value = false
            }
        }
    }







    fun setVoiceTranscripts(enabled: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                settingsRepository.setVoiceTranscriptsEnabled(enabled)
                android.util.Log.d("ChatSettingsViewModel", "Voice transcripts ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                android.util.Log.e("ChatSettingsViewModel", "Failed to toggle voice transcripts", e)
                _error.value = "Failed to update voice transcripts"
            } finally {
                _isLoading.value = false
            }
        }
    }







    fun setAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                settingsRepository.setAutoBackupEnabled(enabled)
                android.util.Log.d("ChatSettingsViewModel", "Auto backup ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                android.util.Log.e("ChatSettingsViewModel", "Failed to toggle auto backup", e)
                _error.value = "Failed to update auto backup"
            } finally {
                _isLoading.value = false
            }
        }
    }







    fun clearError() {
        _error.value = null
    }
}
