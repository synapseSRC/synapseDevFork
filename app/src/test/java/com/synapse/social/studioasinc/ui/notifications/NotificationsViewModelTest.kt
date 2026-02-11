package com.synapse.social.studioasinc.ui.notifications

import com.synapse.social.studioasinc.shared.domain.model.Notification
import com.synapse.social.studioasinc.shared.domain.model.NotificationMessageType
import com.synapse.social.studioasinc.shared.domain.usecase.notification.GetNotificationsUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.notification.MarkNotificationAsReadUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.notification.SubscribeToNotificationsUseCase
import com.synapse.social.studioasinc.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class NotificationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(testDispatcher)

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var getNotificationsUseCase: GetNotificationsUseCase

    @Mock
    lateinit var markNotificationAsReadUseCase: MarkNotificationAsReadUseCase

    @Mock
    lateinit var subscribeToNotificationsUseCase: SubscribeToNotificationsUseCase

    private lateinit var viewModel: NotificationsViewModel

    private fun createViewModel() {
        viewModel = NotificationsViewModel(
            getNotificationsUseCase,
            markNotificationAsReadUseCase,
            subscribeToNotificationsUseCase
        )
    }

    private fun mockNotification(id: String, isRead: Boolean = false): Notification {
        return Notification(
            id = id,
            type = "like",
            actorName = "Actor",
            actorAvatar = null,
            message = "Message",
            messageType = NotificationMessageType.CUSTOM,
            timestamp = "2024-01-01T12:00:00Z",
            isRead = isRead,
            targetId = null
        )
    }

    @Test
    fun `initialization should load notifications`() = runTest(testDispatcher) {
        val notifications = listOf(mockNotification("1"), mockNotification("2"))
        whenever(getNotificationsUseCase()).thenReturn(flowOf(Result.success(notifications)))
        whenever(subscribeToNotificationsUseCase()).thenReturn(flowOf())

        createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.notifications.size)
        assertFalse(state.isLoading)
        verify(getNotificationsUseCase).invoke()
        verify(subscribeToNotificationsUseCase).invoke()
    }

    @Test
    fun `loadNotifications failure should handle error`() = runTest(testDispatcher) {
        whenever(getNotificationsUseCase()).thenReturn(flowOf(Result.failure(Exception("Network error"))))

        createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.notifications.isEmpty())
        assertFalse(state.isLoading)
        verify(getNotificationsUseCase).invoke()
        verify(subscribeToNotificationsUseCase, never()).invoke()
    }

    @Test
    fun `markAsRead should call use case and update state optimistically`() = runTest(testDispatcher) {
        val notificationId = "1"
        val notifications = listOf(mockNotification(notificationId, isRead = false))
        whenever(getNotificationsUseCase()).thenReturn(flowOf(Result.success(notifications)))
        whenever(subscribeToNotificationsUseCase()).thenReturn(flowOf())
        whenever(markNotificationAsReadUseCase(notificationId)).thenReturn(Result.success(Unit))

        createViewModel()
        advanceUntilIdle()

        viewModel.markAsRead(notificationId)
        runCurrent()

        // Optimistic update happens immediately
        val state = viewModel.uiState.value
        assertTrue(state.notifications.first().isRead)

        advanceUntilIdle()
        verify(markNotificationAsReadUseCase).invoke(notificationId)
    }

    @Test
    fun `markAsRead failure should revert state`() = runTest(testDispatcher) {
        val notificationId = "1"
        val notifications = listOf(mockNotification(notificationId, isRead = false))
        whenever(getNotificationsUseCase()).thenReturn(flowOf(Result.success(notifications)))
        whenever(subscribeToNotificationsUseCase()).thenReturn(flowOf())
        whenever(markNotificationAsReadUseCase(notificationId)).thenReturn(Result.failure(Exception("Error")))

        createViewModel()
        advanceUntilIdle()

        viewModel.markAsRead(notificationId)
        runCurrent()

        // Revert should happen after failure
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.notifications.first().isRead)
    }

    @Test
    fun `realtime notification should update state`() = runTest(testDispatcher) {
        val notifications = listOf(mockNotification("1"))
        whenever(getNotificationsUseCase()).thenReturn(flowOf(Result.success(notifications)))

        val newNotification = mockNotification("2")
        whenever(subscribeToNotificationsUseCase()).thenReturn(flowOf(newNotification))

        createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.notifications.size)
        assertEquals("2", state.notifications.first().id) // Newest first usually
    }
}
