package com.synapse.social.studioasinc.feature.shared.main

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.synapse.social.studioasinc.shared.data.repository.AuthRepository
import com.synapse.social.studioasinc.shared.data.repository.UserRepository
import com.synapse.social.studioasinc.ui.navigation.AppDestination
import com.synapse.social.studioasinc.shared.domain.model.User
import com.synapse.social.studioasinc.shared.domain.model.UserStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlinx.coroutines.flow.flow
import io.github.jan.supabase.auth.status.SessionStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class MainViewModelPerformanceTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    @Mock
    lateinit var application: Application

    @Mock
    lateinit var authRepository: AuthRepository

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var connectivityManager: ConnectivityManager

    @Mock
    lateinit var networkCapabilities: NetworkCapabilities

    private lateinit var viewModel: MainViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        whenever(application.getSystemService(ConnectivityManager::class.java)).thenReturn(connectivityManager)
        whenever(connectivityManager.activeNetwork).thenReturn(Mockito.mock(android.net.Network::class.java))
        whenever(connectivityManager.getNetworkCapabilities(any())).thenReturn(networkCapabilities)
        whenever(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `checkUserAuthentication completes faster with reactive flow`() = runTest(testDispatcher) {
        val userId = "user123"
        whenever(authRepository.getCurrentUserId()).thenReturn(userId)
        whenever(authRepository.getCurrentUserEmail()).thenReturn("test@test.com")

        // Mock sessionStatus flow: Loading -> NotAuthenticated (after 100ms)
        whenever(authRepository.sessionStatus).thenReturn(flow {
            delay(100)
            emit(SessionStatus.NotAuthenticated())
        })

        whenever(authRepository.restoreSession()).thenReturn(true)

        val user = User(
            id = userId,
            uid = userId,
            email = "test@test.com",
            username = "testuser",
            bio = "",
            avatar = "",
            createdAt = "",
            followersCount = 0,
            followingCount = 0,
            banned = false,
            accountPremium = false,
            userLevelXp = 0,
            verify = false,
            accountType = "user",
            gender = "hidden",
            status = UserStatus.OFFLINE,
            joinDate = "",
            oneSignalPlayerId = null,
            lastSeen = null,
            chattingWith = null,
            updatedAt = null,
            postsCount = 0
        )
        whenever(userRepository.getUserById(userId)).thenReturn(Result.success(user))

        viewModel = MainViewModel(application, authRepository, userRepository)

        advanceUntilIdle()

        println("Virtual time elapsed: ${currentTime}ms")
        // It should take exactly 100ms (the simulated delay)
        assertEquals(100L, currentTime)
    }
}
