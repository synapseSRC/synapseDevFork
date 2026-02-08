package com.synapse.social.studioasinc.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.synapse.social.studioasinc.data.remote.services.SupabaseAuthenticationService

/**
 * Factory for creating InboxViewModel instances with dependencies.
 * Requirements: 2.4
 */
class InboxViewModelFactory(
    private val authService: SupabaseAuthenticationService = SupabaseAuthenticationService()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InboxViewModel::class.java)) {
            return InboxViewModel(
                authService = authService
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
