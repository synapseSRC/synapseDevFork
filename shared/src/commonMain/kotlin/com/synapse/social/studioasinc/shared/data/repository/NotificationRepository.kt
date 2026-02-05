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
import kotlinx.serialization.json.JsonObject
import io.github.aakira.napier.Napier
import com.synapse.social.studioasinc.shared.core.util.getCurrentIsoTime

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
}
