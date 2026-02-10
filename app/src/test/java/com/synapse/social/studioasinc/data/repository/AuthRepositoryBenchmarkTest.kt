package com.synapse.social.studioasinc.data.repository

import com.synapse.social.studioasinc.shared.data.repository.AuthRepository as SharedAuthRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import kotlin.system.measureNanoTime

class AuthRepositoryBenchmarkTest {

    @Mock
    lateinit var sharedAuthRepository: SharedAuthRepository

    lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        authRepository = AuthRepository(sharedAuthRepository)
    }

    @Test
    fun getCurrentUserId_shouldBeFastAndNonBlocking() {
        // Arrange
        val expectedUserId = "user_12345"
        Mockito.`when`(sharedAuthRepository.getCurrentUserId()).thenReturn(expectedUserId)

        // Act & Measure
        val time = measureNanoTime {
            val actualUserId = authRepository.getCurrentUserId()
            assertEquals(expectedUserId, actualUserId)
        }

        // Assert
        println("getCurrentUserId execution time: ${time}ns")
        // Check if it runs in under 1ms (1,000,000 ns)
        // This is a loose check because CI environments vary, but < 1ms is expected for a simple wrapper call.
        assert(time < 50_000_000) { "Execution took too long: ${time}ns" } // 50ms threshold to be safe on slow CI
    }

    @Test
    fun getCurrentUserEmail_shouldBeFastAndNonBlocking() {
        // Arrange
        val expectedEmail = "test@example.com"
        Mockito.`when`(sharedAuthRepository.getCurrentUserEmail()).thenReturn(expectedEmail)

        // Act & Measure
        val time = measureNanoTime {
            val actualEmail = authRepository.getCurrentUserEmail()
            assertEquals(expectedEmail, actualEmail)
        }

        // Assert
        println("getCurrentUserEmail execution time: ${time}ns")
        assert(time < 50_000_000) { "Execution took too long: ${time}ns" }
    }
}
