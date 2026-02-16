package com.synapse.social.studioasinc.feature.shared.viewmodel

import com.synapse.social.studioasinc.data.remote.services.SupabaseFollowService
import com.synapse.social.studioasinc.shared.data.repository.AuthRepository
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
class FollowButtonViewModelTest {

    @Mock
    lateinit var authRepository: AuthRepository

    @Mock
    lateinit var followService: SupabaseFollowService

    private lateinit var viewModel: FollowButtonViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = FollowButtonViewModel(authRepository, followService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be not following and not loading`() {
        val state = viewModel.uiState.value
        assertFalse(state.isFollowing)
        assertFalse(state.isLoading)
    }

    @Test
    fun `initialize should check follow status and update state when user is logged in`() = runTest {

        val currentUid = "currentUid"
        val targetUid = "targetUid"

        whenever(authRepository.getCurrentUserUid()).thenReturn(currentUid)
        whenever(followService.isFollowing(currentUid, targetUid)).thenReturn(Result.success(true))


        viewModel.initialize(targetUid)
        advanceUntilIdle()


        val state = viewModel.uiState.value
        assertTrue(state.isFollowing)
        assertFalse(state.isLoading)
        verify(followService).isFollowing(currentUid, targetUid)
    }

    @Test
    fun `initialize should not check follow status when user is not logged in`() = runTest {

        whenever(authRepository.getCurrentUserUid()).thenReturn(null)
        val targetUid = "targetUid"


        viewModel.initialize(targetUid)
        advanceUntilIdle()


        verify(followService, never()).isFollowing(any(), any())
        val state = viewModel.uiState.value
        assertFalse(state.isFollowing)
    }

    @Test
    fun `initialize should handle failure when checking follow status`() = runTest {

        val currentUid = "currentUid"
        val targetUid = "targetUid"
        whenever(authRepository.getCurrentUserUid()).thenReturn(currentUid)
        whenever(followService.isFollowing(currentUid, targetUid)).thenReturn(Result.failure(Exception("Network error")))


        viewModel.initialize(targetUid)
        advanceUntilIdle()


        val state = viewModel.uiState.value
        assertFalse(state.isFollowing)
        assertFalse(state.isLoading)
        verify(followService).isFollowing(currentUid, targetUid)
    }

    @Test
    fun `initialize should not check follow status if target user is the current user`() = runTest {

        val uid = "sameUid"
        whenever(authRepository.getCurrentUserUid()).thenReturn(uid)


        viewModel.initialize(uid)
        advanceUntilIdle()


        verify(followService, never()).isFollowing(any(), any())
        val state = viewModel.uiState.value
        assertFalse(state.isFollowing)
        assertFalse(state.isLoading)
    }

    @Test
    fun `toggleFollow should call followUser when not following`() = runTest {

        val currentUid = "currentUid"
        val targetUid = "targetUid"

        whenever(authRepository.getCurrentUserUid()).thenReturn(currentUid)
        whenever(followService.isFollowing(currentUid, targetUid)).thenReturn(Result.success(false))
        whenever(followService.followUser(currentUid, targetUid)).thenReturn(Result.success(Unit))


        viewModel.initialize(targetUid)
        advanceUntilIdle()


        viewModel.toggleFollow()
        advanceUntilIdle()


        verify(followService).followUser(currentUid, targetUid)
        assertTrue(viewModel.uiState.value.isFollowing)
    }

    @Test
    fun `toggleFollow should call unfollowUser when already following`() = runTest {

        val currentUid = "currentUid"
        val targetUid = "targetUid"

        whenever(authRepository.getCurrentUserUid()).thenReturn(currentUid)
        whenever(followService.isFollowing(currentUid, targetUid)).thenReturn(Result.success(true))
        whenever(followService.unfollowUser(currentUid, targetUid)).thenReturn(Result.success(Unit))


        viewModel.initialize(targetUid)
        advanceUntilIdle()


        viewModel.toggleFollow()
        advanceUntilIdle()


        verify(followService).unfollowUser(currentUid, targetUid)
        assertFalse(viewModel.uiState.value.isFollowing)
    }

    @Test
    fun `toggleFollow should handle failure gracefully`() = runTest {

        val currentUid = "currentUid"
        val targetUid = "targetUid"

        whenever(authRepository.getCurrentUserUid()).thenReturn(currentUid)
        whenever(followService.isFollowing(currentUid, targetUid)).thenReturn(Result.success(false))
        whenever(followService.followUser(currentUid, targetUid)).thenReturn(Result.failure(Exception("Network error")))


        viewModel.initialize(targetUid)
        advanceUntilIdle()


        viewModel.toggleFollow()
        advanceUntilIdle()


        verify(followService).followUser(currentUid, targetUid)
        assertFalse(viewModel.uiState.value.isFollowing)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
