package com.synapse.social.studioasinc.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.minus
import kotlinx.datetime.DatePeriod

/**
 * ViewModel for the Business Platform screen.
 *
 * Manages state for business features, analytics, monetization, and verification.
 */
class BusinessPlatformViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(
        BusinessPlatformState(
            accountType = AccountType.PERSONAL,
            isBusinessAccount = false,
            analytics = null,
            monetizationEnabled = false,
            revenue = null,
            verificationStatus = VerificationStatus.NOT_APPLIED
        )
    )
    val state: StateFlow<BusinessPlatformState> = _state.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadBusinessData()
    }

    fun loadBusinessData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val supabaseClient = com.synapse.social.studioasinc.core.network.SupabaseClient.client
                val user = supabaseClient.auth.currentUserOrNull()

                if (user != null) {
                    // Fetch Business Account Info
                    val businessAccounts = supabaseClient.from("business_accounts")
                        .select {
                            filter {
                                eq("user_id", user.id)
                            }
                        }.decodeList<BusinessAccountDto>()

                    val businessAccount = businessAccounts.firstOrNull()

                    val accountType = if (businessAccount != null) {
                        try {
                            AccountType.valueOf(businessAccount.account_type.uppercase())
                        } catch (e: Exception) {
                            AccountType.PERSONAL
                        }
                    } else {
                        AccountType.PERSONAL
                    }

                    // Fetch Analytics (Mocked for now as per requirements, or fetch from analytics_daily)
                    val analytics = fetchAnalytics(user.id)

                    // Fetch Revenue
                    val revenue = fetchRevenue(user.id)

                    _state.value = _state.value.copy(
                        accountType = accountType,
                        isBusinessAccount = accountType != AccountType.PERSONAL,
                        monetizationEnabled = businessAccount?.monetization_enabled ?: false,
                        verificationStatus = try {
                            VerificationStatus.valueOf((businessAccount?.verification_status ?: "NOT_APPLIED").uppercase())
                        } catch (e: Exception) {
                            VerificationStatus.NOT_APPLIED
                        },
                        analytics = analytics,
                        revenue = revenue
                    )
                }
            } catch (e: Exception) {
                _error.value = "Failed to load business data: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchAnalytics(userId: String): AnalyticsData {
        // In a real implementation, query 'analytics_daily' table
        // For now, returning dummy data for visualization
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val points = (0..6).map { dayOffset ->
             DataPoint(
                 date = today.minus(DatePeriod(days = 6 - dayOffset)).toString(),
                 value = (100..500).random().toFloat()
             )
        }

        return AnalyticsData(
            profileViews = 1250,
            engagementRate = 4.5f,
            followerGrowth = points,
            topPosts = listOf(
                PostAnalytics("1", "Summer Vibes", 5000),
                PostAnalytics("2", "Tech Talk", 3200),
                PostAnalytics("3", "Tutorial", 2800)
            )
        )
    }

    private suspend fun fetchRevenue(userId: String): RevenueData {
        // Query 'revenue_transactions' table
        return RevenueData(
            totalEarnings = 1500.00,
            pendingPayout = 250.00,
            lastPayoutDate = System.currentTimeMillis() - 86400000 * 5 // 5 days ago
        )
    }

    fun switchToBusinessAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val supabaseClient = com.synapse.social.studioasinc.core.network.SupabaseClient.client
                val user = supabaseClient.auth.currentUserOrNull() ?: return@launch

                val newAccount = BusinessAccountDto(
                    user_id = user.id,
                    account_type = "BUSINESS",
                    monetization_enabled = false,
                    verification_status = "NOT_APPLIED"
                )

                supabaseClient.from("business_accounts").upsert(newAccount)
                loadBusinessData()
            } catch (e: Exception) {
                _error.value = "Failed to switch account type: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleMonetization(enabled: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val supabaseClient = com.synapse.social.studioasinc.core.network.SupabaseClient.client
                val user = supabaseClient.auth.currentUserOrNull() ?: return@launch

                supabaseClient.from("business_accounts").update(
                    {
                        set("monetization_enabled", enabled)
                    }
                ) {
                    filter {
                        eq("user_id", user.id)
                    }
                }

                _state.value = _state.value.copy(monetizationEnabled = enabled)
            } catch (e: Exception) {
                _error.value = "Failed to update monetization settings: ${e.message}"
                // Revert state if failed
                loadBusinessData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyForVerification() {
        viewModelScope.launch {
             _isLoading.value = true
            try {
                val supabaseClient = com.synapse.social.studioasinc.core.network.SupabaseClient.client
                val user = supabaseClient.auth.currentUserOrNull() ?: return@launch

                supabaseClient.from("business_accounts").update(
                    {
                        set("verification_status", "PENDING")
                    }
                ) {
                    filter {
                        eq("user_id", user.id)
                    }
                }

                _state.value = _state.value.copy(verificationStatus = VerificationStatus.PENDING)
            } catch (e: Exception) {
                _error.value = "Failed to apply for verification: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

// ============================================================================
// Data Models
// ============================================================================

data class BusinessPlatformState(
    val accountType: AccountType,
    val isBusinessAccount: Boolean,
    val analytics: AnalyticsData?,
    val monetizationEnabled: Boolean,
    val revenue: RevenueData?,
    val verificationStatus: VerificationStatus
)

data class AnalyticsData(
    val profileViews: Int,
    val engagementRate: Float,
    val followerGrowth: List<DataPoint>,
    val topPosts: List<PostAnalytics>
)

data class RevenueData(
    val totalEarnings: Double,
    val pendingPayout: Double,
    val lastPayoutDate: Long?
)

data class DataPoint(
    val date: String,
    val value: Float
)

data class PostAnalytics(
    val postId: String,
    val title: String,
    val views: Int
)

enum class AccountType {
    PERSONAL, CREATOR, BUSINESS
}

enum class VerificationStatus {
    NOT_APPLIED, PENDING, VERIFIED, REJECTED
}

@Serializable
data class BusinessAccountDto(
    val user_id: String,
    val account_type: String,
    val monetization_enabled: Boolean,
    val verification_status: String
)
