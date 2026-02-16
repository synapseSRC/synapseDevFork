package com.synapse.social.studioasinc.feature.shared.main

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.synapse.social.studioasinc.shared.data.repository.AuthRepository
import com.synapse.social.studioasinc.data.repository.UserRepository
import com.synapse.social.studioasinc.ui.navigation.AppDestination
import com.synapse.social.studioasinc.domain.model.User
import com.synapse.social.studioasinc.domain.model.UserStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
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
    fun `checkUserAuthentication waits unnecessarily long when session is ready early`() = runTest(testDispatcher) {
        // Arrange: Session becomes available after 100ms
        // Initially false
        var sessionReady = false

        // Mock restoreSession to return true only after 100ms
        whenever(authRepository.restoreSession()).thenAnswer {
            sessionReady
        }

        // Let's set up the mocks for success path
        val userId = "user123"
        whenever(authRepository.getCurrentUserId()).thenReturn(userId)
        whenever(authRepository.getCurrentUserEmail()).thenReturn("test@test.com")

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

        // Act
        // We launch a background coroutine to set sessionReady = true after 100ms
        val job = launch {
            delay(100)
            sessionReady = true
        }

        viewModel = MainViewModel(application, authRepository, userRepository)

        // Run pending tasks
        advanceUntilIdle()

        // Assert
        println("Virtual time elapsed: ${currentTime}ms")

        // We assert that it took at least 500ms (because of the hardcoded delay)
        // Even though the session was ready at 100ms.
        assertEquals(500L, currentTime)

        job.cancel()
    }
}
