package com.synapse.social.studioasinc.ui.settings

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.exceptions.CreateCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.core.network.SupabaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.Base64
import java.util.UUID

@Serializable
data class PasskeyItem(
    val id: String,
    val deviceName: String,
    val dateAdded: Long,
    val lastUsed: Long? = null
)

@Serializable
data class PasskeyTableItem(
    val id: String,
    val user_id: String,
    val credential_id: String,
    val device_name: String,
    val date_added: Long,
    val last_used: Long? = null
)

@HiltViewModel
class PasskeysViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val PASSKEY_VERIFICATION_FUNCTION = "verify-passkey-registration"
    }

    private val _passkeys = MutableStateFlow<List<PasskeyItem>>(emptyList())
    val passkeys: StateFlow<List<PasskeyItem>> = _passkeys.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val credentialManager = CredentialManager.create(context)

    init {
        loadPasskeys()
    }

    fun loadPasskeys() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch
                val result = SupabaseClient.client.from("user_passkeys")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<PasskeyTableItem>()

                _passkeys.value = result.map {
                    PasskeyItem(
                        id = it.id,
                        deviceName = it.device_name,
                        dateAdded = it.date_added,
                        lastUsed = it.last_used
                    )
                }
            } catch (e: Exception) {
                Log.e("PasskeysViewModel", "Error loading passkeys", e)
                _error.value = "Failed to load passkeys: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPasskey(activityContext: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val userId = user?.id ?: run {
                    _error.value = "User not logged in"
                    return@launch
                }
                val userEmail = user?.email ?: "user"




                val challenge = SupabaseClient.client.functions.invoke("generate-passkey-challenge").body<Map<String, String>>()["challenge"] ?: throw IllegalStateException("Invalid challenge response")
                val userIdEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(userId.toByteArray())





                val rpId = "synapse-social.com"

                val requestJson = """
                {
                    "challenge": "$challenge",
                    "rp": {
                        "name": "Synapse Social",
                        "id": "$rpId"
                    },
                    "user": {
                        "id": "$userIdEncoded",
                        "name": "$userEmail",
                        "displayName": "$userEmail"
                    },
                    "pubKeyCredParams": [
                        { "type": "public-key", "alg": -7 },
                        { "type": "public-key", "alg": -257 }
                    ],
                    "timeout": 60000,
                    "attestation": "direct",
                    "authenticatorSelection": {
                        "authenticatorAttachment": "platform",
                        "requireResidentKey": true,
                        "residentKey": "required",
                        "userVerification": "required"
                    }
                }
                """.trimIndent()

                Log.d("PasskeysViewModel", "Requesting credential creation with JSON: $requestJson")

                try {
                    val request = CreatePublicKeyCredentialRequest(requestJson)

                    val response = credentialManager.createCredential(activityContext, request)
                    if (response is androidx.credentials.CreatePublicKeyCredentialResponse) {
                        Log.d("PasskeysViewModel", "Credential created successfully: ${response.registrationResponseJson}")
                        registerPasskeyOnServer(response.registrationResponseJson)
                    } else {
                        _error.value = "Unexpected response type from Credential Manager"
                    }

                } catch (e: CreateCredentialException) {
                    Log.e("PasskeysViewModel", "Credential Manager Error", e)
                    _error.value = "Passkey creation failed: ${e.message}"
                }

            } catch (e: Exception) {
                Log.e("PasskeysViewModel", "Error adding passkey", e)
                _error.value = "Error adding passkey: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun registerPasskeyOnServer(registrationResponseJson: String) {
        try {
            _isLoading.value = true
            val result = SupabaseClient.client.functions.invoke(
                function = PASSKEY_VERIFICATION_FUNCTION,
                body = mapOf(
                    "registrationResponse" to registrationResponseJson,
                    "deviceName" to Build.MODEL
                )
            )

            if (result.status.value in 200..299) {
                Log.d("PasskeysViewModel", "Passkey registered successfully on server")
                loadPasskeys()
            } else {
                val errorBody = result.body<Map<String, String>>()
                val errorMessage = errorBody["error"] ?: "Server returned status ${result.status.value}"
                Log.e("PasskeysViewModel", "Server registration failed: $errorMessage")
                _error.value = "Failed to register passkey on server: $errorMessage"
            }
        } catch (e: Exception) {
            Log.e("PasskeysViewModel", "Error during server registration", e)
            _error.value = "Failed to register passkey on server: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun removePasskey(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                SupabaseClient.client.from("user_passkeys").delete {
                    filter {
                        eq("id", id)
                    }
                }
                loadPasskeys()
            } catch (e: Exception) {
                Log.e("PasskeysViewModel", "Error removing passkey", e)
                _error.value = "Failed to remove passkey: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
