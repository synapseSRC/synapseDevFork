package com.synapse.social.studioasinc.ui.notifications

import com.synapse.social.studioasinc.data.repository.AuthRepository
import com.synapse.social.studioasinc.shared.data.repository.NotificationRepository
import com.synapse.social.studioasinc.shared.data.model.NotificationDto
import com.synapse.social.studioasinc.shared.data.model.NotificationActorDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NotificationsViewModelTest {

    @Mock lateinit var authRepository: AuthRepository
    @Mock lateinit var notificationRepository: NotificationRepository

    private lateinit var viewModel: NotificationsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        whenever(authRepository.getCurrentUserId()).thenReturn("test-user-id")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization should load notifications`() = runTest {
        val notifications = listOf(createFakeNotificationDto("1"))
        whenever(notificationRepository.fetchNotifications(any(), any())).thenReturn(notifications)

        viewModel = NotificationsViewModel(authRepository, notificationRepository)

        verify(notificationRepository).fetchNotifications(eq("test-user-id"), any())
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.notifications.size)
        assertEquals("1", state.notifications[0].id)
    }

    @Test
    fun `loadNotifications success should update state`() = runTest {
        val notifications = listOf(
            createFakeNotificationDto("1", isRead = false),
            createFakeNotificationDto("2", isRead = true)
        )
        whenever(notificationRepository.fetchNotifications(any(), any())).thenReturn(notifications)

        viewModel = NotificationsViewModel(authRepository, notificationRepository)

        val state = viewModel.uiState.value
        assertEquals(2, state.notifications.size)
        assertEquals(1, state.unreadCount)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadNotifications failure should handle error gracefully`() = runTest {
        whenever(notificationRepository.fetchNotifications(any(), any())).thenThrow(RuntimeException("Network error"))

        viewModel = NotificationsViewModel(authRepository, notificationRepository)

        val state = viewModel.uiState.value
        assertTrue(state.notifications.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `markAsRead should perform optimistic update and call repository`() = runTest {
        val notifications = listOf(createFakeNotificationDto("1", isRead = false))
        whenever(notificationRepository.fetchNotifications(any(), any())).thenReturn(notifications)

        viewModel = NotificationsViewModel(authRepository, notificationRepository)

        viewModel.markAsRead("1")

        // Check optimistic update
        val state = viewModel.uiState.value
        assertTrue(state.notifications[0].isRead)
        assertEquals(0, state.unreadCount)

        // Verify repository call
        verify(notificationRepository).markAsRead("test-user-id", "1")
    }

    @Test
    fun `refresh should trigger reload`() = runTest {
        whenever(notificationRepository.fetchNotifications(any(), any())).thenReturn(emptyList())

        viewModel = NotificationsViewModel(authRepository, notificationRepository)
        clearInvocations(notificationRepository)

        viewModel.refresh()

        verify(notificationRepository).fetchNotifications(eq("test-user-id"), any())
    }

    private fun createFakeNotificationDto(
        id: String,
        isRead: Boolean = false
    ): NotificationDto {
        return NotificationDto(
            id = id,
            recipientId = "test-user-id",
            senderId = "sender-id",
            type = "like",
            title = buildJsonObject { put("en", "Title") },
            body = buildJsonObject { put("en", "Body") },
            createdAt = "2023-01-01T00:00:00Z",
            isRead = isRead,
            actor = NotificationActorDto(displayName = "Actor", avatar = null)
        )
    }
}
