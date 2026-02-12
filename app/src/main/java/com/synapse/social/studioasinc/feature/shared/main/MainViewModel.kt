package com.synapse.social.studioasinc.feature.shared.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.feature.shared.navigation.AppDestination
import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val session = SupabaseClient.client.auth.currentSessionOrNull()
            if (session != null) {
                _startDestination.value = AppDestination.Home.route
            } else {
                _startDestination.value = AppDestination.Auth.route
            }
        }
    }
}
