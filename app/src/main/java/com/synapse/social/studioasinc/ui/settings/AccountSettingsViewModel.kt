package com.synapse.social.studioasinc.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.booleanOrNull



class AccountSettingsViewModel(application: Application) : AndroidViewModel(application) {





    private val _linkedAccounts = MutableStateFlow<LinkedAccountsState>(LinkedAccountsState())
    val linkedAccounts: StateFlow<LinkedAccountsState> = _linkedAccounts.asStateFlow()

    private val _securityNotificationsEnabled = MutableStateFlow(true)
    val securityNotificationsEnabled: StateFlow<Boolean> = _securityNotificationsEnabled.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()


    private val _showChangeEmailDialog = MutableStateFlow(false)
    val showChangeEmailDialog: StateFlow<Boolean> = _showChangeEmailDialog.asStateFlow()

    private val _showChangePasswordDialog = MutableStateFlow(false)
    val showChangePasswordDialog: StateFlow<Boolean> = _showChangePasswordDialog.asStateFlow()

    private val _showDeleteAccountDialog = MutableStateFlow(false)
    val showDeleteAccountDialog: StateFlow<Boolean> = _showDeleteAccountDialog.asStateFlow()

    init {
        loadLinkedAccounts()
        loadSecurityNotificationsSettings()
    }







    private fun loadSecurityNotificationsSettings() {
        viewModelScope.launch {
            try {
                val supabaseClient = com.synapse.social.studioasinc.core.network.SupabaseClient.client
                val currentUser = supabaseClient.auth.currentUserOrNull()

                if (currentUser != null) {
                    val response = supabaseClient.from("user_preferences").select {
                        filter { eq("user_id", currentUser.id) }
                    }.decodeSingleOrNull<JsonObject>()

                    if (response != null) {
                         _securityNotificationsEnabled.value = response["security_notifications_enabled"]?.jsonPrimitive?.booleanOrNull ?: true
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AccountSettingsViewModel", "Failed to load security notifications settings", e)

            }
        }
    }



    fun toggleSecurityNotifications(enabled: Boolean) {
        viewModelScope.launch {

            _securityNotificationsEnabled.value = enabled

            try {
                val supabaseClient = com.synapse.social.studioasinc.core.network.SupabaseClient.client
                val currentUser = supabaseClient.auth.currentUserOrNull()

                if (currentUser != null) {
                    supabaseClient.from("user_preferences").upsert(
                        mapOf(
                            "user_id" to currentUser.id,
                            "security_notifications_enabled" to enabled
                        )
                    ) {
                        onConflict = "user_id"
                    }
                }
            } catch (e: Exception) {

                _securityNotificationsEnabled.value = !enabled
                android.util.Log.e("AccountSettingsViewModel", "Failed to update security notifications", e)
                _error.value = "Failed to update security notifications"
            }
        }
    }







    private fun loadLinkedAccounts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {

                val supabaseClient = com.synapse.social.studioasinc.core.network.SupabaseClient.client


                val identities = supabaseClient.from("user_identities")
                    .select()
                    .decodeList<JsonObject>()

                _linkedAccounts.value = LinkedAccountsState(
                    googleLinked = identities.any { it["provider"]?.jsonPrimitive?.contentOrNull == "google" },
                    facebookLinked = identities.any { it["provider"]?.jsonPrimitive?.contentOrNull == "facebook" },
                    appleLinked = identities.any { it["provider"]?.jsonPrimitive?.contentOrNull == "apple" }
                )


                android.util.Log.d("AccountSettingsViewModel", "Loaded ${identities.size} linked accounts")
            } catch (e: Exception) {
                android.util.Log.e("AccountSettingsViewModel", "Failed to load linked accounts", e)
                _error.value = "Failed to load linked accounts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun connectSocialAccount(provider: SocialProvider) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val supabaseClient = com.synapse.social.studioasinc.core.network.SupabaseClient.client

                when (provider) {
                    SocialProvider.GOOGLE -> {
                        supabaseClient.auth.linkIdentity(io.github.jan.supabase.auth.providers.Google)
                    }
                    SocialProvider.FACEBOOK -> {
                        supabaseClient.auth.linkIdentity(io.github.jan.supabase.auth.providers.Facebook)
                    }
                    SocialProvider.APPLE -> {

                        throw UnsupportedOperationException("Apple linking not yet supported")
                    }
                }


                loadLinkedAccounts()
            } catch (e: Exception) {
                android.util.Log.e("AccountSettingsViewModel", "Failed to connect $provider", e)
                _error.value = "Failed to connect ${provider.displayName}: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun disconnectSocialAccount(provider: SocialProvider) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val supabaseClient = com.synapse.social.studioasinc.core.network.SupabaseClient.client
                val currentUser = supabaseClient.auth.currentUserOrNull()

                if (currentUser != null) {
                    val identities = currentUser.identities ?: emptyList()
                    val targetIdentity = identities.find {
                        it.provider == provider.name.lowercase()
                    }

                    if (targetIdentity != null) {
                        supabaseClient.auth.unlinkIdentity(targetIdentity.id)

                        loadLinkedAccounts()
                    } else {
                        _error.value = "${provider.displayName} account is not linked"
                    }
                } else {
                    _error.value = "User not authenticated"
                }
            } catch (e: Exception) {
                android.util.Log.e("AccountSettingsViewModel", "Failed to disconnect $provider", e)
                _error.value = "Failed to disconnect ${provider.displayName}: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }







    fun showChangeEmailDialog() {
        _showChangeEmailDialog.value = true
    }



    fun dismissChangeEmailDialog() {
        _showChangeEmailDialog.value = false
        _error.value = null
    }



    fun changeEmail(newEmail: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {

                if (!isValidEmail(newEmail)) {
                    _error.value = "Invalid email format"
                    return@launch
                }

                if (password.isBlank()) {
                    _error.value = "Password is required"
                    return@launch
                }


                val supabaseClient = com.synapse.social.studioasinc.core.network.SupabaseClient.client
                val currentUser = supabaseClient.auth.currentUserOrNull()
                val currentEmail = currentUser?.email

                if (currentEmail == null) {
                    _error.value = "User email not found. Please sign in again."
                    return@launch
                }


                try {
                    supabaseClient.auth.signInWith(Email) {
                        this.email = currentEmail
                        this.password = password
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AccountSettingsViewModel", "Failed to verify password", e)
                    if (e.message?.contains("invalid", ignoreCase = true) == true ||
                        e.message?.contains("credential", ignoreCase = true) == true) {
                        _error.value = "Incorrect password"
                    } else {
                        _error.value = "Failed to verify password: ${e.message}"
                    }
                    return@launch
                }


                android.util.Log.d("AccountSettingsViewModel", "Changing email to: $newEmail")
                supabaseClient.auth.updateUser {
                    email = newEmail
                }

                _showChangeEmailDialog.value = false
            } catch (e: Exception) {
                android.util.Log.e("AccountSettingsViewModel", "Failed to change email", e)
                _error.value = "Failed to change email: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }







    fun showChangePasswordDialog() {
        _showChangePasswordDialog.value = true
    }



    fun dismissChangePasswordDialog() {
        _showChangePasswordDialog.value = false
        _error.value = null
    }



    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {

                if (currentPassword.isBlank()) {
                    _error.value = "Current password is required"
                    return@launch
                }

                if (newPassword.length < 8) {
                    _error.value = "New password must be at least 8 characters"
                    return@launch
                }

                if (newPassword != confirmPassword) {
                    _error.value = "Passwords do not match"
                    return@launch
                }


                val supabaseClient = com.synapse.social.studioasinc.core.network.SupabaseClient.client
                val currentUser = supabaseClient.auth.currentUserOrNull()
                val email = currentUser?.email

                if (email == null) {
                    _error.value = "User email not found. Please sign in again."
                    return@launch
                }


                try {
                    supabaseClient.auth.signInWith(Email) {
                        this.email = email
                        this.password = currentPassword
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AccountSettingsViewModel", "Failed to verify password", e)

                    if (e.message?.contains("invalid", ignoreCase = true) == true ||
                        e.message?.contains("credential", ignoreCase = true) == true) {
                        _error.value = "Incorrect current password"
                    } else {
                        _error.value = "Failed to verify password: ${e.message}"
                    }
                    return@launch
                }


                supabaseClient.auth.updateUser {
                    password = newPassword
                }

                android.util.Log.d("AccountSettingsViewModel", "Password changed successfully")

                _showChangePasswordDialog.value = false
            } catch (e: Exception) {
                android.util.Log.e("AccountSettingsViewModel", "Failed to change password", e)
                _error.value = "Failed to change password: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun calculatePasswordStrength(password: String): Int {
        var strength = 0

        if (password.length >= 8) strength++
        if (password.length >= 12) strength++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) strength++
        if (password.any { it.isDigit() }) strength++
        if (password.any { !it.isLetterOrDigit() }) strength++

        return strength.coerceIn(0, 4)
    }







    fun showDeleteAccountDialog() {
        _showDeleteAccountDialog.value = true
    }



    fun dismissDeleteAccountDialog() {
        _showDeleteAccountDialog.value = false
        _error.value = null
    }



    fun deleteAccount(confirmationText: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {

                if (confirmationText != DELETE_ACCOUNT_CONFIRMATION) {
                    _error.value = "Please type the exact confirmation phrase"
                    return@launch
                }

                android.util.Log.d("AccountSettingsViewModel", "Deleting account")

                val supabaseClient = com.synapse.social.studioasinc.core.network.SupabaseClient.client


                supabaseClient.functions.invoke(function = "delete-account")


                supabaseClient.auth.signOut()

                _showDeleteAccountDialog.value = false


            } catch (e: Exception) {
                android.util.Log.e("AccountSettingsViewModel", "Failed to delete account", e)
                _error.value = "Failed to delete account: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }







    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }



    fun clearError() {
        _error.value = null
    }

    companion object {
        const val DELETE_ACCOUNT_CONFIRMATION = "DELETE MY ACCOUNT"
    }
}



data class LinkedAccountsState(
    val googleLinked: Boolean = false,
    val facebookLinked: Boolean = false,
    val appleLinked: Boolean = false
)



enum class SocialProvider {
    GOOGLE,
    FACEBOOK,
    APPLE;

    val displayName: String
        get() = when (this) {
            GOOGLE -> "Google"
            FACEBOOK -> "Facebook"
            APPLE -> "Apple"
        }
}
