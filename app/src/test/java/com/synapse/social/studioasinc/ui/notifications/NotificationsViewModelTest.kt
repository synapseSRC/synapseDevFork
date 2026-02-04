package com.synapse.social.studioasinc.ui.notifications

import com.synapse.social.studioasinc.data.repository.AuthRepository
import com.synapse.social.studioasinc.shared.data.model.NotificationActorDto
import com.synapse.social.studioasinc.shared.data.model.NotificationDto
import com.synapse.social.studioasinc.shared.data.repository.NotificationRepository
import com.synapse.social.studioasinc.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [NotificationsViewModel] verifying state transitions,
 * notification loading, and optimistic updates for marking as read.
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NotificationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(testDispatcher)

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var authRepository: AuthRepository

    @Mock
    lateinit var notificationRepository: NotificationRepository

    private lateinit var viewModel: NotificationsViewModel

    private fun createViewModel() {
        viewModel = NotificationsViewModel(authRepository, notificationRepository)
    }

    @Test
    fun `initialization should load notifications for current user`() = runTest(testDispatcher) {
        // Arrange
        val userId = "user123"
        whenever(authRepository.getCurrentUserId()).thenReturn(userId)
        val mockNotifications = listOf(
            createMockNotificationDto("1", "like"),
            createMockNotificationDto("2", "comment")
        )
        whenever(notificationRepository.fetchNotifications(userId)).thenReturn(mockNotifications)

        // Act
        createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse("Loading should be false after completion", state.isLoading)
        assertEquals("Should have 2 notifications", 2, state.notifications.size)
        assertEquals("Should have 2 unread notifications", 2, state.unreadCount)

        // Robust assertion: Find by ID instead of index
        val likeNotif = state.notifications.first { it.id == "1" }
        assertEquals("Notification with ID '1' should have correct message", "Message for like", likeNotif.message)

        verify(notificationRepository).fetchNotifications(userId)
    }

    @Test
    fun `initialization with no user should set loading to false and not fetch`() = runTest(testDispatcher) {
        // Arrange
        whenever(authRepository.getCurrentUserId()).thenReturn(null)

        // Act
        createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse("Loading should be false even if no user", state.isLoading)
        assertTrue("Notifications list should be empty", state.notifications.isEmpty())
        verifyNoInteractions(notificationRepository)
    }

    @Test
    fun `loadNotifications failure should handle exception and stop loading`() = runTest(testDispatcher) {
        // Arrange
        val userId = "user123"
        whenever(authRepository.getCurrentUserId()).thenReturn(userId)
        whenever(notificationRepository.fetchNotifications(userId)).thenThrow(RuntimeException("Network error"))

        // Act
        createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse("Loading should be false after error", state.isLoading)
        assertTrue("Notifications list should be empty on error", state.notifications.isEmpty())
    }

    @Test
    fun `markAsRead should perform optimistic update and call repository`() = runTest(testDispatcher) {
        // Arrange
        val userId = "user123"
        val notificationId = "notif1"
        whenever(authRepository.getCurrentUserId()).thenReturn(userId)
        val mockNotifications = listOf(createMockNotificationDto(notificationId, "like", isRead = false))
        whenever(notificationRepository.fetchNotifications(userId)).thenReturn(mockNotifications)

        createViewModel()
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.unreadCount)

        // Act
        viewModel.markAsRead(notificationId)

        // Assert Optimistic Update (Immediate state change)
        runCurrent() // Execute up to the suspension point (optimistic update happens before repo call)
        val stateAfterAct = viewModel.uiState.value
        assertTrue("Notification should be marked as read optimistically", stateAfterAct.notifications.first { it.id == notificationId }.isRead)
        assertEquals("Unread count should decrease optimistically", 0, stateAfterAct.unreadCount)

        // Advance to complete repository call
        advanceUntilIdle()
        verify(notificationRepository).markAsRead(userId, notificationId)
    }

    @Test
    fun `markAsRead failure should revert state by reloading notifications`() = runTest(testDispatcher) {
        // Arrange
        val userId = "user123"
        val notificationId = "notif1"
        whenever(authRepository.getCurrentUserId()).thenReturn(userId)
        val mockNotifications = listOf(createMockNotificationDto(notificationId, "like", isRead = false))
        whenever(notificationRepository.fetchNotifications(userId)).thenReturn(mockNotifications)

        createViewModel()
        advanceUntilIdle()

        // Mock repository failure
        whenever(notificationRepository.markAsRead(userId, notificationId)).thenThrow(RuntimeException("Update failed"))

        // Act
        viewModel.markAsRead(notificationId)
        runCurrent() // Apply optimistic update
        advanceUntilIdle() // Complete the flow (including failure and revert)

        // Assert - verify fetchNotifications was called again to revert (once in init, once in revert)
        verify(notificationRepository, times(2)).fetchNotifications(userId)
    }

    @Test
    fun `markAsRead failure followed by reload failure should handle gracefully`() = runTest(testDispatcher) {
        // Arrange
        val userId = "user123"
        val notificationId = "notif1"
        whenever(authRepository.getCurrentUserId()).thenReturn(userId)
        val mockNotifications = listOf(createMockNotificationDto(notificationId, "like", isRead = false))
        whenever(notificationRepository.fetchNotifications(userId)).thenReturn(mockNotifications)

        createViewModel()
        advanceUntilIdle()

        // Mock both failures
        whenever(notificationRepository.markAsRead(userId, notificationId)).thenThrow(RuntimeException("Update failed"))
        whenever(notificationRepository.fetchNotifications(userId)).thenThrow(RuntimeException("Revert failed"))

        // Act
        viewModel.markAsRead(notificationId)
        runCurrent() // Apply optimistic update
        advanceUntilIdle() // Fail repo call and fail reload

        // Assert
        val state = viewModel.uiState.value
        assertFalse("Loading should be false after all attempts", state.isLoading)

        // Strengthening assertion: In current implementation, the state REMAINS isRead = true
        // if reload fails, because there's no manual rollback in markAsRead catch.
        // This confirms the limitation/edge case identified in review.
        val notif = state.notifications.first { it.id == notificationId }
        assertFalse("UI state should be reverted even if reload fails", notif.isRead)

        verify(notificationRepository, times(2)).fetchNotifications(userId)
    }

    @Test
    fun `refresh should trigger reload of notifications`() = runTest(testDispatcher) {
        // Arrange
        val userId = "user123"
        whenever(authRepository.getCurrentUserId()).thenReturn(userId)
        whenever(notificationRepository.fetchNotifications(userId)).thenReturn(emptyList())

        createViewModel()
        advanceUntilIdle()
        verify(notificationRepository, times(1)).fetchNotifications(userId)

        // Act
        viewModel.refresh()
        advanceUntilIdle()

        // Assert
        verify(notificationRepository, times(2)).fetchNotifications(userId)
    }

    private fun createMockNotificationDto(
        id: String,
        type: String,
        isRead: Boolean = false
    ): NotificationDto {
        return NotificationDto(
            id = id,
            recipientId = "user123",
            type = type,
            title = buildJsonObject { put("en", "Title") },
            body = buildJsonObject { put("en", "Message for $type") },
            createdAt = "2024-05-24T12:00:00Z",
            isRead = isRead,
            actor = NotificationActorDto(displayName = "Actor Name", avatar = "avatar_url")
        )
    }
}
