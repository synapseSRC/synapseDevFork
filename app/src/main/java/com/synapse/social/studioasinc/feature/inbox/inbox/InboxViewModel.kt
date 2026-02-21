package com.synapse.social.studioasinc.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.shared.domain.model.User
import com.synapse.social.studioasinc.UserProfileManager
import com.synapse.social.studioasinc.shared.data.auth.SupabaseAuthenticationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch



class InboxViewModel(
    private val authService: SupabaseAuthenticationService = SupabaseAuthenticationService()
) : ViewModel() {


    private val _currentUserProfile = MutableStateFlow<User?>(null)
    val currentUserProfile: StateFlow<User?> = _currentUserProfile.asStateFlow()

    init {
        viewModelScope.launch {
            loadCurrentUserProfile()
        }
    }

    private suspend fun loadCurrentUserProfile() {
        try {
            val profile = UserProfileManager.getCurrentUserProfile()
            _currentUserProfile.value = profile
        } catch (e: Exception) {

        }
    }
}
