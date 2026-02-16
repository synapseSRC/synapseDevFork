package com.synapse.social.studioasinc.feature.auth.presentation.viewmodel

import android.content.SharedPreferences
import com.synapse.social.studioasinc.shared.data.repository.AuthRepository
import com.synapse.social.studioasinc.data.repository.UsernameRepository
import com.synapse.social.studioasinc.feature.auth.ui.models.AuthNavigationEvent
import com.synapse.social.studioasinc.feature.auth.ui.models.AuthUiState
import com.synapse.social.studioasinc.feature.auth.ui.models.PasswordStrength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AuthViewModelTest {

    @Mock lateinit var authRepository: AuthRepository
    @Mock lateinit var usernameRepository: UsernameRepository
    @Mock lateinit var sharedPreferences: SharedPreferences
    @Mock lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.putString(any(), any())).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.apply()).then {}

        whenever(authRepository.observeAuthState()).thenReturn(flowOf(false))
        whenever(authRepository.getCurrentUserId()).thenReturn(null)
        whenever(authRepository.getCurrentUserEmail()).thenReturn(null)

        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be SignIn`() = runTest {
        viewModel = AuthViewModel(authRepository, usernameRepository, sharedPreferences)
        assertTrue(viewModel.uiState.value is AuthUiState.SignIn)
    }

    @Test
    fun `onEmailChanged should update email in state`() = runTest {
        viewModel = AuthViewModel(authRepository, usernameRepository, sharedPreferences)

        val email = "test@example.com"
        viewModel.onEmailChanged(email)

        val state = viewModel.uiState.value as AuthUiState.SignIn
        assertEquals(email, state.email)
    }

    @Test
    fun `calculatePasswordStrength should return correct strength`() = runTest {
        viewModel = AuthViewModel(authRepository, usernameRepository, sharedPreferences)

        assertEquals(PasswordStrength.Weak, viewModel.calculatePasswordStrength("123"))
        assertEquals(PasswordStrength.Fair, viewModel.calculatePasswordStrength("password123"))
        assertEquals(PasswordStrength.Strong, viewModel.calculatePasswordStrength("Password123!"))
    }

    @Test
    fun `onSignInClick failure should show error`() = runTest {
        viewModel = AuthViewModel(authRepository, usernameRepository, sharedPreferences)

        val email = "test@example.com"
        val password = "password123"
        val errorMessage = "Error"
        whenever(authRepository.signIn(eq(email), eq(password))).thenReturn(Result.failure(Exception(errorMessage)))

        viewModel.onSignInClick(email, password)

        val state = viewModel.uiState.value as AuthUiState.SignIn
        assertEquals(errorMessage, state.generalError)
    }
}
