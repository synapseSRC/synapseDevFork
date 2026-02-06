package com.synapse.social.studioasinc.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.synapse.social.studioasinc.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PhoneChangeStep {
    ENTER_PHONE,
    VERIFY_CODE,
    SUCCESS
}

data class PhoneChangeState(
    val currentPhone: String? = null,
    val countryCode: String = "+1",
    val phoneNumber: String = "",
    val verificationCode: String = "",
    val step: PhoneChangeStep = PhoneChangeStep.ENTER_PHONE,
    val isLoading: Boolean = false,
    val error: String? = null,
    val resendTimer: Int = 0,
    val canResend: Boolean = true
)

@HiltViewModel
class ChangePhoneNumberViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PhoneChangeState())
    val state: StateFlow<PhoneChangeState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private val phoneUtil = PhoneNumberUtil.getInstance()

    fun onCountryCodeChange(code: String) {
        val cleanCode = if (code.startsWith("+")) code else "+$code"
        _state.update { it.copy(countryCode = cleanCode, error = null) }
    }

    fun onPhoneNumberChange(number: String) {
        // Filter mainly digits
        val filtered = number.filter { it.isDigit() || it == '-' || it == ' ' }
        _state.update { it.copy(phoneNumber = filtered, error = null) }
    }

    fun onVerificationCodeChange(code: String) {
        if (code.length <= 6 && code.all { it.isDigit() }) {
            _state.update { it.copy(verificationCode = code, error = null) }
        }
    }

    fun sendVerificationCode() {
        val fullNumber = getFormattedNumber()

        // Validate phone number
        try {
            val numberProto = phoneUtil.parse(fullNumber, null)
            if (!phoneUtil.isValidNumber(numberProto)) {
                _state.update { it.copy(error = "Invalid phone number format") }
                return
            }
        } catch (e: Exception) {
            _state.update { it.copy(error = "Invalid phone number: ${e.message}") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            // Note: Supabase automatically sends the OTP when updating the user phone
            val result = authRepository.updatePhoneNumber(fullNumber)

            _state.update { it.copy(isLoading = false) }

            if (result.isSuccess) {
                _state.update { it.copy(step = PhoneChangeStep.VERIFY_CODE) }
                startResendTimer()
            } else {
                _state.update { it.copy(error = result.exceptionOrNull()?.message ?: "Failed to send code") }
            }
        }
    }

    fun verifyCode() {
        val code = _state.value.verificationCode
        val fullNumber = getFormattedNumber()

        if (code.length != 6) {
            _state.update { it.copy(error = "Code must be 6 digits") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.verifyPhoneChange(fullNumber, code)

            _state.update { it.copy(isLoading = false) }

            if (result.isSuccess) {
                _state.update { it.copy(step = PhoneChangeStep.SUCCESS) }
            } else {
                _state.update { it.copy(error = result.exceptionOrNull()?.message ?: "Verification failed") }
            }
        }
    }

    fun resendCode() {
        if (!_state.value.canResend) return
        sendVerificationCode()
    }

    private fun getFormattedNumber(): String {
        return "${_state.value.countryCode}${_state.value.phoneNumber}".replace(" ", "").replace("-", "")
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _state.update { it.copy(resendTimer = 60, canResend = false) }
            repeat(60) {
                delay(1000)
                _state.update { s -> s.copy(resendTimer = s.resendTimer - 1) }
            }
            _state.update { it.copy(canResend = true) }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
