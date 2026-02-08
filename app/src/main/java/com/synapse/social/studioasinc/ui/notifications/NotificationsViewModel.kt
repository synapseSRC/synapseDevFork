package com.synapse.social.studioasinc.ui.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.R
import com.synapse.social.studioasinc.core.util.TimeUtils
import com.synapse.social.studioasinc.data.repository.AuthRepository
import com.synapse.social.studioasinc.shared.data.repository.NotificationRepository
import com.synapse.social.studioasinc.shared.data.model.NotificationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Immutable
data class NotificationsUiState(
    val notifications: List<UiNotification> = emptyList(),
    val isLoading: Boolean = false,
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
    @ApplicationContext private val context: Context
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
                    val notifications = dtos.map { mapDtoToUi(it) }

                     _uiState.update {
                        it.copy(
                            notifications = notifications,
                            isLoading = false,
                            unreadCount = notifications.count { !it.isRead }
                        )
                    }


                    launch {
                        try {
                            notificationRepository.getRealtimeNotifications(currentUserId).collect { dto ->
                                val newNotification = mapDtoToUi(dto)
                                _uiState.update { state ->
                                    state.copy(
                                        notifications = listOf(newNotification) + state.notifications,
                                        unreadCount = state.unreadCount + 1
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("NotificationsViewModel", "Realtime error", e)
                        }
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

    private fun mapDtoToUi(dto: NotificationDto): UiNotification {
        val fallback = context.getString(R.string.notification_fallback_message)
        val newActivity = context.getString(R.string.notification_new_activity)

        return UiNotification(
            id = dto.id,
            type = dto.type,
            actorName = dto.actor?.displayName ?: newActivity,
            actorAvatar = dto.actor?.avatar,
            message = dto.body["en"]?.jsonPrimitive?.contentOrNull ?: fallback,
            timestamp = TimeUtils.getTimeAgo(dto.createdAt),
            isRead = dto.isRead,
            targetId = dto.data?.get("target_id")?.jsonPrimitive?.contentOrNull
        )
    }

    fun refresh() {
        loadNotifications()
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch


            setNotificationReadState(notificationId, isRead = true)

            try {
                notificationRepository.markAsRead(userId, notificationId)
            } catch (e: Exception) {
                android.util.Log.e("NotificationsViewModel", "Failed to mark as read for notification $notificationId", e)

                setNotificationReadState(notificationId, isRead = false)
            }
        }
    }

    private fun setNotificationReadState(notificationId: String, isRead: Boolean) {
        _uiState.update { state ->
            val index = state.notifications.indexOfFirst { it.id == notificationId }
            if (index == -1 || state.notifications[index].isRead == isRead) return@update state

            val updatedList = state.notifications.toMutableList()
            updatedList[index] = updatedList[index].copy(isRead = isRead)

            val newUnreadCount = if (isRead) {
                (state.unreadCount - 1).coerceAtLeast(0)
            } else {
                state.unreadCount + 1
            }

            state.copy(
                notifications = updatedList,
                unreadCount = newUnreadCount
            )
        }
    }
}
