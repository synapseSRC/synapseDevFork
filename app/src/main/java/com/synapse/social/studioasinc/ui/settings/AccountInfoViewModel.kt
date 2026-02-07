package com.synapse.social.studioasinc.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.core.network.SupabaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@HiltViewModel
class AccountInfoViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<AccountInfoState>(AccountInfoState.Loading)
    val uiState: StateFlow<AccountInfoState> = _uiState.asStateFlow()

    fun loadAccountInfo() {
        viewModelScope.launch {
            _uiState.value = AccountInfoState.Loading
            try {
                val client = SupabaseClient.client
                val authUser = client.auth.currentUserOrNull()

                if (authUser == null) {
                    _uiState.value = AccountInfoState.Error("User not logged in")
                    return@launch
                }

                val userId = authUser.id

                // Fetch profile from 'users' table
                val profileResult = client.from("users").select {
                    filter {
                        eq("uid", userId)
                    }
                }.decodeSingleOrNull<UserProfileDto>()

                if (profileResult == null) {
                    _uiState.value = AccountInfoState.Error("Profile not found")
                    return@launch
                }

                // Fetch counts
                // Using select with count(Count.EXACT) inside DSL and limiting columns to 'id'
                val postsCount = client.from("posts").select(columns = Columns.list("id")) {
                    count(Count.EXACT)
                    filter { eq("author_uid", userId) }
                }.countOrNull() ?: 0

                val followersCount = client.from("follows").select(columns = Columns.list("id")) {
                    count(Count.EXACT)
                    filter { eq("following_id", userId) }
                }.countOrNull() ?: 0

                val followingCount = client.from("follows").select(columns = Columns.list("id")) {
                    count(Count.EXACT)
                    filter { eq("follower_id", userId) }
                }.countOrNull() ?: 0

                val storiesCount = client.from("stories").select(columns = Columns.list("id")) {
                    count(Count.EXACT)
                    filter { eq("user_id", userId) }
                }.countOrNull() ?: 0

                val reelsCount = client.from("reels").select(columns = Columns.list("id")) {
                    count(Count.EXACT)
                    filter { eq("creator_id", userId) }
                }.countOrNull() ?: 0

                val accountData = AccountInfoData(
                    userId = userId,
                    username = profileResult.username ?: "N/A",
                    displayName = profileResult.displayName ?: "N/A",
                    email = profileResult.email ?: authUser.email ?: "N/A",
                    phoneNumber = authUser.phone,
                    bio = profileResult.bio,
                    accountType = if (profileResult.accountPremium == true) SubscriptionType.PLUS else SubscriptionType.FREE,
                    isVerified = profileResult.verify == true,
                    createdAt = profileResult.createdAt,
                    lastLoginAt = authUser.lastSignInAt,
                    postsCount = postsCount.toInt(),
                    followersCount = followersCount.toInt(),
                    followingCount = followingCount.toInt(),
                    storiesCount = storiesCount.toInt(),
                    reelsCount = reelsCount.toInt(),
                    region = profileResult.region ?: "Unknown",
                    language = "English" // Placeholder as language preference might be local
                )

                _uiState.value = AccountInfoState.Success(accountData)

            } catch (e: Exception) {
                _uiState.value = AccountInfoState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun copyUserId(context: Context, userId: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("User ID", userId)
        clipboard.setPrimaryClip(clip)
    }
}

@Serializable
data class UserProfileDto(
    val uid: String,
    val username: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val email: String? = null,
    val bio: String? = null,
    @SerialName("account_premium") val accountPremium: Boolean? = false,
    val verify: Boolean? = false,
    val region: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

sealed class AccountInfoState {
    object Loading : AccountInfoState()
    data class Success(val data: AccountInfoData) : AccountInfoState()
    data class Error(val message: String) : AccountInfoState()
}

data class AccountInfoData(
    val userId: String,
    val username: String,
    val displayName: String,
    val email: String,
    val phoneNumber: String?,
    val bio: String?,
    val accountType: SubscriptionType,
    val isVerified: Boolean,
    val createdAt: String?,
    val lastLoginAt: Instant?,
    val postsCount: Int,
    val followersCount: Int,
    val followingCount: Int,
    val storiesCount: Int,
    val reelsCount: Int,
    val region: String,
    val language: String
)

enum class SubscriptionType {
    FREE, PLUS
}
