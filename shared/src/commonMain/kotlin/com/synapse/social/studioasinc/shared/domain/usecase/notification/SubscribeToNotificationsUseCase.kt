package com.synapse.social.studioasinc.shared.domain.usecase.notification
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.data.mapper.toDomain
import com.synapse.social.studioasinc.shared.data.repository.AuthRepository
import com.synapse.social.studioasinc.shared.data.repository.NotificationRepository
import com.synapse.social.studioasinc.shared.domain.model.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import io.github.aakira.napier.Napier

class SubscribeToNotificationsUseCase(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Notification> {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            Napier.w("Cannot subscribe to notifications: User not logged in")
            return emptyFlow()
        }

        return notificationRepository.getRealtimeNotifications(userId)
            .map { it.toDomain() }
    }
}
