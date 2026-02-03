package com.synapse.social.studioasinc.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.data.repository.AuthRepository
import com.synapse.social.studioasinc.shared.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

data class NotificationsUiState(
    val notifications: List<UiNotification> = emptyList(),
    val isLoading: Boolean = false,
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUserId = authRepository.getCurrentUserId()

                if (currentUserId != null) {
                    val dtos = notificationRepository.fetchNotifications(currentUserId)

                    val notifications = dtos.map { dto ->
                        UiNotification(
                            id = dto.id,
                            type = dto.type,
                            actorName = dto.actor?.displayName ?: "User",
                            actorAvatar = dto.actor?.avatar,
                            message = dto.body["en"]?.jsonPrimitive?.contentOrNull ?: "New notification",
                            timestamp = dto.createdAt,
                            isRead = dto.isRead,
                            targetId = dto.data?.get("target_id")?.jsonPrimitive?.contentOrNull
                        )
                    }

                     _uiState.update {
                        it.copy(
                            notifications = notifications,
                            isLoading = false,
                            unreadCount = notifications.count { !it.isRead }
                        )
                    }
                } else {
                     _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationsViewModel", "Failed to load notifications", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refresh() {
        loadNotifications()
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch

            // Exit early if the notification is already marked as read or not found.
            if (_uiState.value.notifications.find { it.id == notificationId }?.isRead == true) {
                return@launch
            }

            // Helper to update the read state of a notification to avoid duplicating logic.
            val updateReadState = { isRead: Boolean ->
                _uiState.update { state ->
                    val targetIndex = state.notifications.indexOfFirst { it.id == notificationId }

                    if (targetIndex == -1 || state.notifications[targetIndex].isRead == isRead) {
                        state // Notification not found or already in the desired state.
                    } else {
                        val updatedList = state.notifications.toMutableList().apply {
                            this[targetIndex] = this[targetIndex].copy(isRead = isRead)
                        }
                        val newUnreadCount = if (isRead) state.unreadCount - 1 else state.unreadCount + 1
                        state.copy(
                            notifications = updatedList,
                            unreadCount = newUnreadCount.coerceAtLeast(0)
                        )
                    }
                }
            }

            // Optimistic update
            updateReadState(true)

            try {
                notificationRepository.markAsRead(userId, notificationId)
            } catch (e: Exception) {
                android.util.Log.e("NotificationsViewModel", "Failed to mark as read", e)
                // Revert on failure
                updateReadState(false)
            }
        }
    }
}
