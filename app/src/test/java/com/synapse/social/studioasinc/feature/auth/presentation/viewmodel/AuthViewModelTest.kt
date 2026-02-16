package com.synapse.social.studioasinc.feature.auth.presentation.viewmodel

import com.synapse.social.studioasinc.feature.auth.ui.models.AuthUiState
import com.synapse.social.studioasinc.shared.domain.model.ValidationResult
import com.synapse.social.studioasinc.shared.domain.usecase.auth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class AuthViewModelTest {

    @Mock lateinit var validateEmailUseCase: ValidateEmailUseCase
    @Mock lateinit var validatePasswordUseCase: ValidatePasswordUseCase
    @Mock lateinit var validateUsernameUseCase: ValidateUsernameUseCase
    @Mock lateinit var calculatePasswordStrengthUseCase: CalculatePasswordStrengthUseCase
    @Mock lateinit var checkUsernameAvailabilityUseCase: CheckUsernameAvailabilityUseCase
    @Mock lateinit var signInUseCase: SignInUseCase
    @Mock lateinit var signUpUseCase: SignUpUseCase
    @Mock lateinit var sendPasswordResetUseCase: SendPasswordResetUseCase
    @Mock lateinit var resetPasswordUseCase: ResetPasswordUseCase
    @Mock lateinit var resendVerificationEmailUseCase: ResendVerificationEmailUseCase
    @Mock lateinit var handleOAuthCallbackUseCase: HandleOAuthCallbackUseCase
    @Mock lateinit var getOAuthUrlUseCase: GetOAuthUrlUseCase
    @Mock lateinit var signInWithOAuthUseCase: SignInWithOAuthUseCase
    @Mock lateinit var refreshSessionUseCase: RefreshSessionUseCase
    @Mock lateinit var isEmailVerifiedUseCase: IsEmailVerifiedUseCase

    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        viewModel = AuthViewModel(
            validateEmailUseCase,
            validatePasswordUseCase,
            validateUsernameUseCase,
            calculatePasswordStrengthUseCase,
            checkUsernameAvailabilityUseCase,
            signInUseCase,
            signUpUseCase,
            sendPasswordResetUseCase,
            resetPasswordUseCase,
            resendVerificationEmailUseCase,
            handleOAuthCallbackUseCase,
            getOAuthUrlUseCase,
            signInWithOAuthUseCase,
            refreshSessionUseCase,
            isEmailVerifiedUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be SignIn`() = runTest {
        assertTrue(viewModel.uiState.value is AuthUiState.SignIn)
    }

    @Test
    fun `onEmailChanged should update email in state`() = runTest {
        val email = "test@example.com"
        whenever(validateEmailUseCase(email)).thenReturn(ValidationResult.Valid)

        viewModel.onEmailChanged(email)

        val state = viewModel.uiState.value as AuthUiState.SignIn
        assertEquals(email, state.email)
        assertTrue(state.isEmailValid)
    }

    @Test
    fun `onSignInClick failure should show error`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val errorMessage = "Error"

        whenever(validateEmailUseCase(email)).thenReturn(ValidationResult.Valid)
        whenever(validatePasswordUseCase(password)).thenReturn(ValidationResult.Valid)
        whenever(signInUseCase(email, password)).thenReturn(Result.failure(Exception(errorMessage)))

        // set initial state
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)

        viewModel.onSignInClick()

        val state = viewModel.uiState.value as AuthUiState.SignIn
        assertEquals(errorMessage, state.generalError)
    }
}
