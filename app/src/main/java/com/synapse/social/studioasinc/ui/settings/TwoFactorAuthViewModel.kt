package com.synapse.social.studioasinc.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.core.network.SupabaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.mfa.Mfa
import io.github.jan.supabase.auth.mfa.mfa
import io.github.jan.supabase.auth.mfa.FactorType
import io.github.jan.supabase.auth.mfa.FactorStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class TwoFactorState(
    val isEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val qrCodeUri: String? = null,
    val secretKey: String? = null,
    val factorId: String? = null,
    val backupCodes: List<String> = emptyList(),
    val isSetupMode: Boolean = false,
    val verificationCode: String = ""
)

@HiltViewModel
class TwoFactorAuthViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _twoFactorState = MutableStateFlow(TwoFactorState())
    val twoFactorState: StateFlow<TwoFactorState> = _twoFactorState.asStateFlow()

    // Access Supabase Client
    private val supabase = SupabaseClient.client

    init {
        loadTwoFactorStatus()
    }

    fun loadTwoFactorStatus() {
        viewModelScope.launch {
            _twoFactorState.value = _twoFactorState.value.copy(isLoading = true, error = null)
            try {
                // List verified factors
                val factors = supabase.auth.mfa.listFactors()
                val totpFactor = factors.totp.firstOrNull { it.status == FactorStatus.VERIFIED }

                _twoFactorState.value = _twoFactorState.value.copy(
                    isEnabled = totpFactor != null,
                    factorId = totpFactor?.id,
                    isLoading = false
                )
            } catch (e: Exception) {
                // e.g. Network error or session expired
                _twoFactorState.value = _twoFactorState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load 2FA status"
                )
            }
        }
    }

    fun startSetup() {
        viewModelScope.launch {
            _twoFactorState.value = _twoFactorState.value.copy(isLoading = true, error = null)
            try {
                val factor = supabase.auth.mfa.enroll(FactorType.TOTP)
                val totpData = factor.totp

                if (totpData != null) {
                    _twoFactorState.value = _twoFactorState.value.copy(
                        qrCodeUri = totpData.qrCode,
                        secretKey = totpData.secret,
                        factorId = factor.id,
                        isSetupMode = true,
                        isLoading = false
                    )
                } else {
                    throw Exception("Failed to get TOTP data")
                }
            } catch (e: Exception) {
                _twoFactorState.value = _twoFactorState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to start setup"
                )
            }
        }
    }

    fun updateVerificationCode(code: String) {
        _twoFactorState.value = _twoFactorState.value.copy(verificationCode = code)
    }

    fun verifyAndEnable() {
        val code = _twoFactorState.value.verificationCode
        val factorId = _twoFactorState.value.factorId ?: return

        viewModelScope.launch {
            _twoFactorState.value = _twoFactorState.value.copy(isLoading = true, error = null)
            try {
                val challenge = supabase.auth.mfa.challenge(factorId)
                supabase.auth.mfa.verify(factorId, challenge.id, code)

                // Successful verification
                _twoFactorState.value = _twoFactorState.value.copy(
                    isEnabled = true,
                    isSetupMode = false,
                    isLoading = false,
                    verificationCode = ""
                )
                generateBackupCodes() // Generate codes after success
            } catch (e: Exception) {
                _twoFactorState.value = _twoFactorState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Verification failed"
                )
            }
        }
    }

    fun generateBackupCodes() {
        // Generate random 8-char codes
        val codes = List(10) {
            (1..8).map { Random.nextInt(0, 36).let { if (it < 10) it.toString() else ('a' + it - 10).toString() } }.joinToString("")
        }

        _twoFactorState.value = _twoFactorState.value.copy(backupCodes = codes)
        // Ideally, these should be hashed and stored in the backend here.
    }

    fun disable2FA() {
        val factorId = _twoFactorState.value.factorId ?: return

        viewModelScope.launch {
            _twoFactorState.value = _twoFactorState.value.copy(isLoading = true, error = null)
            try {
                supabase.auth.mfa.unenroll(factorId)
                _twoFactorState.value = _twoFactorState.value.copy(
                    isEnabled = false,
                    factorId = null,
                    isLoading = false,
                    backupCodes = emptyList()
                )
            } catch (e: Exception) {
                _twoFactorState.value = _twoFactorState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to disable 2FA"
                )
            }
        }
    }

    fun cancelSetup() {
        val factorId = _twoFactorState.value.factorId
        // Only unenroll if we are in setup mode (not enabled yet)
        if (factorId != null && !_twoFactorState.value.isEnabled) {
             viewModelScope.launch {
                 try {
                     supabase.auth.mfa.unenroll(factorId)
                 } catch (e: Exception) {
                     // Ignore error on cleanup
                 }
             }
        }
        _twoFactorState.value = _twoFactorState.value.copy(
            isSetupMode = false,
            qrCodeUri = null,
            secretKey = null,
            factorId = null,
            verificationCode = ""
        )
    }
}
