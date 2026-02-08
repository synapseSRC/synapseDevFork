package com.synapse.social.studioasinc.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

/**
 * Privacy and Security Settings screen composable.
 *
 * Displays comprehensive privacy and security options including:
 * - Privacy Checkup: Quick privacy review
 * - Profile Settings: Last seen, profile photo, about, status visibility
 * - Message Privacy: Read receipts, disappearing messages
 * - Group Privacy: Group settings and permissions
 * - Security: App lock, chat lock, blocked contacts
 * - Active Sessions: Session management
 *
 * Uses MediumTopAppBar with back navigation and displays settings in grouped sections.
 *
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
    viewModel: PrivacySecurityViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBlockedUsers: () -> Unit,
    onNavigateToMutedUsers: () -> Unit,
    onNavigateToActiveSessions: () -> Unit
) {
    val privacySettings by viewModel.privacySettings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    PrivacySecurityContent(
        privacySettings = privacySettings,
        isLoading = isLoading,
        error = error,
        onNavigateBack = onNavigateBack,
        onNavigateToBlockedUsers = onNavigateToBlockedUsers,
        onNavigateToMutedUsers = onNavigateToMutedUsers,
        onNavigateToActiveSessions = onNavigateToActiveSessions,
        onClearError = { viewModel.clearError() },
        onReadReceiptsChanged = { viewModel.setReadReceiptsEnabled(it) },
        onAppLockChanged = { viewModel.setAppLockEnabled(it) },
        onChatLockChanged = { viewModel.setChatLockEnabled(it) },
        onProfileVisibilityChanged = { viewModel.setProfileVisibility(it) },
        onContentVisibilityChanged = { viewModel.setContentVisibility(it) },
        onGroupPrivacyChanged = { viewModel.setGroupPrivacy(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityContent(
    privacySettings: PrivacySettings,
    isLoading: Boolean,
    error: String?,
    onNavigateBack: () -> Unit,
    onNavigateToBlockedUsers: () -> Unit,
    onNavigateToMutedUsers: () -> Unit,
    onNavigateToActiveSessions: () -> Unit,
    onClearError: () -> Unit,
    onReadReceiptsChanged: (Boolean) -> Unit,
    onAppLockChanged: (Boolean) -> Unit,
    onChatLockChanged: (Boolean) -> Unit,
    onProfileVisibilityChanged: (ProfileVisibility) -> Unit,
    onContentVisibilityChanged: (ContentVisibility) -> Unit,
    onGroupPrivacyChanged: (GroupPrivacy) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Privacy & Security") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            if (error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = onClearError) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(paddingValues)
                .padding(horizontal = SettingsSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(SettingsSpacing.sectionSpacing)
        ) {
            // Privacy Checkup Section
            item {
                PrivacyCheckupSection(isLoading = isLoading)
            }

            // Profile Privacy Section
            item {
                ProfilePrivacySection(
                    privacySettings = privacySettings,
                    isLoading = isLoading,
                    onProfileVisibilityChanged = onProfileVisibilityChanged,
                    onContentVisibilityChanged = onContentVisibilityChanged
                )
            }

            // Message Privacy Section
            item {
                MessagePrivacySection(
                    readReceiptsEnabled = privacySettings.readReceiptsEnabled,
                    onReadReceiptsChanged = onReadReceiptsChanged,
                    isLoading = isLoading
                )
            }

            // Group Privacy Section
            item {
                GroupPrivacySection(
                    privacySettings = privacySettings,
                    isLoading = isLoading,
                    onGroupPrivacyChanged = onGroupPrivacyChanged
                )
            }

            // Security Section
            item {
                SecuritySection(
                    appLockEnabled = privacySettings.appLockEnabled,
                    onAppLockChanged = onAppLockChanged,
                    chatLockEnabled = privacySettings.chatLockEnabled,
                    onChatLockChanged = onChatLockChanged,
                    isLoading = isLoading
                )
            }

            // Active Sessions Section
            item {
                ActiveSessionsSection(
                    onNavigateToActiveSessions = onNavigateToActiveSessions,
                    isLoading = isLoading
                )
            }

            // Contacts Section (Blocked & Muted)
            item {
                ContactsSection(
                    onNavigateToBlockedUsers = onNavigateToBlockedUsers,
                    onNavigateToMutedUsers = onNavigateToMutedUsers,
                    isLoading = isLoading
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
