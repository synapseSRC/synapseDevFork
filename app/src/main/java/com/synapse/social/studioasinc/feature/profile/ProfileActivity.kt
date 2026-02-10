package com.synapse.social.studioasinc

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synapse.social.studioasinc.feature.profile.ProfileEditActivity
import com.synapse.social.studioasinc.feature.settings.SettingsActivity
import com.synapse.social.studioasinc.core.network.SupabaseClient
import com.synapse.social.studioasinc.data.repository.AuthRepository
import com.synapse.social.studioasinc.feature.profile.profile.ProfileScreen
import com.synapse.social.studioasinc.feature.profile.profile.ProfileViewModel
import com.synapse.social.studioasinc.feature.shared.components.FollowListActivity
import com.synapse.social.studioasinc.ui.settings.AppearanceViewModel
import com.synapse.social.studioasinc.feature.shared.theme.SynapseTheme
import androidx.activity.enableEdgeToEdge
import io.github.jan.supabase.auth.auth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Deprecated("Use ProfileScreen within MainActivity navigation graph instead")
@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()

        val targetUserId = intent.getStringExtra("uid") ?: run {
            finish()
            return
        }

        setContent {

            val appearanceViewModel: AppearanceViewModel = viewModel()
            val appearanceSettings by appearanceViewModel.appearanceSettings.collectAsState()


            val darkTheme = when (appearanceSettings.themeMode) {
                com.synapse.social.studioasinc.ui.settings.ThemeMode.LIGHT -> false
                com.synapse.social.studioasinc.ui.settings.ThemeMode.DARK -> true
                com.synapse.social.studioasinc.ui.settings.ThemeMode.SYSTEM ->
                    isSystemInDarkTheme()
            }


            val dynamicColor = appearanceSettings.dynamicColorEnabled &&
                               Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

            SynapseTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    var currentUserId by remember { mutableStateOf<String?>(null) }
                    var isLoading by remember { mutableStateOf(true) }

                    LaunchedEffect(Unit) {
                        try {
                            val id = withContext(Dispatchers.IO) {
                                SupabaseClient.client.auth.currentUserOrNull()?.id
                            }
                            if (id != null) {
                                currentUserId = id
                            } else {
                                finish()
                            }
                        } catch (e: Exception) {
                            finish()
                        } finally {
                            isLoading = false
                        }
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        currentUserId?.let { uid ->
                            ProfileScreen(
                                userId = targetUserId,
                                currentUserId = uid,
                                onNavigateBack = { finish() },
                                onNavigateToEditProfile = { navigateToEditProfile() },
                                onNavigateToFollowers = { navigateToFollowers(targetUserId) },
                                onNavigateToFollowing = { navigateToFollowing(targetUserId) },
                                onNavigateToSettings = { navigateToSettings() },
                                onNavigateToActivityLog = { navigateToActivityLog() },
                                onNavigateToUserProfile = { userId -> navigateToUserProfile(userId) },
                                onNavigateToChat = { userId -> navigateToChat(userId) },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    private fun navigateToEditProfile() {
        try {
            android.util.Log.d("ProfileActivity", "Attempting to navigate to EditProfile")
            val intent = Intent(this, ProfileEditActivity::class.java)
            android.util.Log.d("ProfileActivity", "Intent created, starting activity")
            startActivity(intent)
            android.util.Log.d("ProfileActivity", "Activity started successfully")
        } catch (e: Exception) {
            android.util.Log.e("ProfileActivity", "Failed to navigate to EditProfile", e)
            Toast.makeText(this, "Unable to open Edit Profile: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToFollowers(userId: String) {
        val intent = Intent(this, FollowListActivity::class.java)
        intent.putExtra(FollowListActivity.EXTRA_USER_ID, userId)
        intent.putExtra(FollowListActivity.EXTRA_LIST_TYPE, FollowListActivity.TYPE_FOLLOWERS)
        startActivity(intent)
    }

    private fun navigateToFollowing(userId: String) {
        val intent = Intent(this, FollowListActivity::class.java)
        intent.putExtra(FollowListActivity.EXTRA_USER_ID, userId)
        intent.putExtra(FollowListActivity.EXTRA_LIST_TYPE, FollowListActivity.TYPE_FOLLOWING)
        startActivity(intent)
    }

    private fun navigateToSettings() {
        try {
            android.util.Log.d("ProfileActivity", "Attempting to navigate to Settings")
            val intent = Intent(this, SettingsActivity::class.java)
            android.util.Log.d("ProfileActivity", "Intent created, starting activity")
            startActivity(intent)
            android.util.Log.d("ProfileActivity", "Settings activity started successfully")
        } catch (e: Exception) {
            android.util.Log.e("ProfileActivity", "Failed to navigate to Settings", e)
            Toast.makeText(this, "Unable to open Settings: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToActivityLog() {
        startActivity(Intent(this, ActivityLogActivity::class.java))
    }

    private fun navigateToUserProfile(userId: String) {
        @Suppress("DEPRECATION")
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("uid", userId)
        startActivity(intent)
    }

    private fun navigateToChat(targetUserId: String) {
        lifecycleScope.launch {
            try {

                val currentUserId = authRepository.getCurrentUserUid()

                if (currentUserId == null) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Failed to get user info",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                if (targetUserId == currentUserId) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "You cannot message yourself",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }


                @Suppress("DEPRECATION")
                Toast.makeText(this@ProfileActivity, "Chat feature not implemented", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.util.Log.e("ProfileActivity", "Error starting chat", e)
                Toast.makeText(
                    this@ProfileActivity,
                    "Error starting chat: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
