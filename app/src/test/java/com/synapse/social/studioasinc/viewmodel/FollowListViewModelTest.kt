package com.synapse.social.studioasinc.viewmodel

import com.synapse.social.studioasinc.data.remote.services.SupabaseFollowService
import com.synapse.social.studioasinc.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
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
class FollowListViewModelTest {

    @Mock
    lateinit var followService: SupabaseFollowService

    private lateinit var viewModel: FollowListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = FollowListViewModel(followService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be empty and not loading`() {
        val state = viewModel.uiState.value
        assertTrue(state.users.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadUsers success should update state with users`() = runTest {
        // Arrange
        val userId = "user123"
        val mockUsers = listOf(
            mapOf("uid" to "follower1", "username" to "user1", "verify" to "true"),
            mapOf("uid" to "follower2", "username" to "user2", "verify" to "false")
        )
        whenever(followService.getFollowers(eq(userId), any())).thenReturn(Result.success(mockUsers))

        // Act
        viewModel.loadUsers(userId, "followers")
        advanceUntilIdle()

        // Assert Success
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.users.size)
        assertEquals("follower1", state.users[0].uid)
    }

    @Test
    fun `loadUsers failure should update state with error`() = runTest {
        // Arrange
        val userId = "user123"
        val errorMessage = "Network Error"
        whenever(followService.getFollowing(eq(userId), any())).thenReturn(Result.failure(Exception(errorMessage)))

        // Act
        viewModel.loadUsers(userId, "following")
        advanceUntilIdle()

        // Assert Failure
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.users.isEmpty())
        assertNotNull(state.error)
        assertTrue(state.error!!.contains(errorMessage))
    }

    @Test
    fun `loadUsers with unknown type should result in empty list and no error`() = runTest(testDispatcher) {
        // Arrange
        val userId = "user123"

        // Act
        viewModel.loadUsers(userId, "some_invalid_type")
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.users.isEmpty())
        assertNull(state.error)
    }
}
