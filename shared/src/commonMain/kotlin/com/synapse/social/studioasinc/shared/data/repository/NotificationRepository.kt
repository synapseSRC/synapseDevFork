package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.data.model.NotificationDto
import com.synapse.social.studioasinc.shared.data.model.NotificationPreferencesDto
import com.synapse.social.studioasinc.shared.data.model.NotificationAnalyticsDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.serialization.json.JsonObject
import io.github.aakira.napier.Napier
import com.synapse.social.studioasinc.shared.core.util.getCurrentIsoTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.jan.supabase.realtime.channel

class NotificationRepository(private val supabase: SupabaseClient) {

    suspend fun fetchNotifications(userId: String, limit: Long = 50): List<NotificationDto> {
        val currentUserId = supabase.auth.currentUserOrNull()?.id
        if (currentUserId != userId) {
            Napier.e("IDOR attempt: User $currentUserId tried to fetch notifications for $userId")
            return emptyList()
        }
        return try {
            supabase.postgrest.from("notifications")
                .select(Columns.raw("*, actor:sender_id(display_name, avatar)")) {
                    filter {
                        eq("recipient_id", userId)
                    }
                    order("created_at", Order.DESCENDING)
                    limit(limit)
                }
                .decodeList<NotificationDto>()
        } catch (e: Exception) {
            Napier.e("Failed to fetch notifications for $userId", e)
            emptyList()
        }
    }

    fun getRealtimeNotifications(userId: String): Flow<NotificationDto> {
        val channel = supabase.channel("notifications:$userId")
        val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "notifications"
            filter("recipient_id", FilterOperator.EQ, userId)
        }

        CoroutineScope(Dispatchers.Default).launch {
            try {
                channel.subscribe()
            } catch (e: Exception) {
                Napier.e("Failed to subscribe to realtime channel", e)
            }
        }

        return flow.map {
            it.decodeRecord<NotificationDto>()
        }
    }

    suspend fun markAsRead(userId: String, notificationId: String) {
        val currentUserId = supabase.auth.currentUserOrNull()?.id
        if (currentUserId != userId) {
            Napier.e("IDOR attempt: User $currentUserId tried to mark notification $notificationId as read for $userId")
            return
        }
        try {
            val now = getCurrentIsoTime()
            supabase.postgrest.from("notifications").update(
                mapOf(
                    "is_read" to true,
                    "read_at" to now
                )
            ) {
                filter {
                    eq("id", notificationId)
                    eq("recipient_id", userId)
                }
            }
        } catch (e: Exception) {
            Napier.e("Failed to mark notification $notificationId as read", e)
        }
    }

    suspend fun fetchPreferences(userId: String): NotificationPreferencesDto? {
        val currentUserId = supabase.auth.currentUserOrNull()?.id
        if (currentUserId != userId) {
            Napier.e("IDOR attempt: User $currentUserId tried to fetch preferences for $userId")
            return null
        }
        return try {
            supabase.postgrest.from("notification_preferences")
                .select() {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<NotificationPreferencesDto>()
        } catch (e: Exception) {
            Napier.e("Failed to fetch preferences for $userId", e)
            null
        }
    }

    suspend fun updatePreferences(userId: String, preferences: NotificationPreferencesDto) {
        val currentUserId = supabase.auth.currentUserOrNull()?.id
        if (currentUserId != userId || userId != preferences.userId) {
            Napier.e("IDOR attempt: User $currentUserId tried to update preferences for ${preferences.userId}")
            return
        }
        try {
            supabase.postgrest.from("notification_preferences")
                .upsert(preferences)
        } catch (e: Exception) {
            Napier.e("Failed to update preferences for ${preferences.userId}", e)
        }
    }

    suspend fun logAnalytics(analytics: NotificationAnalyticsDto) {
        try {
            supabase.postgrest.from("notification_analytics")
                .insert(analytics)
        } catch (e: Exception) {
            Napier.e("Failed to log notification analytics", e)
        }
    }

    suspend fun updateOneSignalPlayerId(userId: String, playerId: String) {
        val currentUserId = supabase.auth.currentUserOrNull()?.id
        if (currentUserId != userId) {
            Napier.e("IDOR attempt: User $currentUserId tried to update OneSignal ID for $userId")
            return
        }
        try {
            supabase.postgrest.from("users").update(
                mapOf("one_signal_player_id" to playerId)
            ) {
                filter { eq("uid", userId) }
            }
        } catch (e: Exception) {
            Napier.e("Failed to update OneSignal ID for $userId", e)
        }
    }
}
