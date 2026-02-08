package com.synapse.social.studioasinc.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.data.remote.services.SupabaseAuthenticationService
import com.synapse.social.studioasinc.ui.inbox.models.*
import com.synapse.social.studioasinc.domain.model.User
import com.synapse.social.studioasinc.UserProfileManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Inbox screen.
 * Note: Chat functionality removed - shows empty state UI
 */
class InboxViewModel(
    private val authService: SupabaseAuthenticationService = SupabaseAuthenticationService()
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<InboxUiState>(InboxUiState.Loading)
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    // Current user profile
    private val _currentUserProfile = MutableStateFlow<User?>(null)
    val currentUserProfile: StateFlow<User?> = _currentUserProfile.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            loadCurrentUserProfile()
            // Show empty state since chat functionality is removed
            _uiState.value = InboxUiState.Success
        }
    }

    private suspend fun loadCurrentUserProfile() {
        try {
            val profile = UserProfileManager.getCurrentUserProfile()
            _currentUserProfile.value = profile
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    /**
     * Handles UI actions
     */
    fun onAction(action: InboxAction) {
        when (action) {
            is InboxAction.Refresh -> loadData()
        }
    }
}
