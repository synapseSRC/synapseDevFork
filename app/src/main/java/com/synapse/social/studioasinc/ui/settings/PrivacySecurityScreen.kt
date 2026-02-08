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
import com.synapse.social.studioasinc.R

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
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error ?: "")
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
                ProfilePrivacySection(isLoading = isLoading)
            }

            // Message Privacy Section
            item {
                MessagePrivacySection(
                    readReceiptsEnabled = privacySettings.readReceiptsEnabled,
                    onReadReceiptsChanged = { viewModel.setReadReceiptsEnabled(it) },
                    isLoading = isLoading
                )
            }

            // Group Privacy Section
            item {
                GroupPrivacySection(isLoading = isLoading)
            }

            // Security Section
            item {
                SecuritySection(
                    appLockEnabled = privacySettings.appLockEnabled,
                    onAppLockChanged = { viewModel.setAppLockEnabled(it) },
                    chatLockEnabled = privacySettings.chatLockEnabled,
                    onChatLockChanged = { viewModel.setChatLockEnabled(it) },
                    isLoading = isLoading
                )
            }

            // Blocked Contacts Section
            item {
                BlockedContactsSection(
                    onNavigateToBlockedUsers = onNavigateToBlockedUsers,
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

@Composable
private fun PrivacyCheckupSection(isLoading: Boolean) {
    SettingsSection(title = "Privacy Checkup") {
        SettingsNavigationItem(
            title = "Privacy Checkup",
            subtitle = "Review your privacy settings",
            icon = R.drawable.ic_security,
            onClick = { },
            enabled = !isLoading
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfilePrivacySection(isLoading: Boolean) {
    SettingsSection(title = "Profile Privacy") {
        SettingsSelectionItem(
            title = "Last Seen",
            subtitle = "Control who can see when you were last online",
            icon = R.drawable.ic_visibility,
            options = listOf("Everyone", "My Contacts", "Nobody"),
            selectedOption = "My Contacts",
            onSelect = { },
            enabled = !isLoading
        )
        SettingsDivider()
        SettingsSelectionItem(
            title = "Profile Photo",
            subtitle = "Control who can see your profile photo",
            icon = R.drawable.ic_person,
            options = listOf("Everyone", "My Contacts", "Nobody"),
            selectedOption = "Everyone",
            onSelect = { },
            enabled = !isLoading
        )
        SettingsDivider()
        SettingsSelectionItem(
            title = "About",
            subtitle = "Control who can see your about info",
            icon = R.drawable.ic_info,
            options = listOf("Everyone", "My Contacts", "Nobody"),
            selectedOption = "Everyone",
            onSelect = { },
            enabled = !isLoading
        )
        SettingsDivider()
        SettingsSelectionItem(
            title = "Status",
            subtitle = "Control who can see your status updates",
            icon = R.drawable.ic_status,
            options = listOf("My Contacts", "My Contacts Except...", "Only Share With..."),
            selectedOption = "My Contacts",
            onSelect = { },
            enabled = !isLoading
        )
    }
}

@Composable
private fun MessagePrivacySection(
    readReceiptsEnabled: Boolean,
    onReadReceiptsChanged: (Boolean) -> Unit,
    isLoading: Boolean
) {
    SettingsSection(title = "Message Privacy") {
        SettingsToggleItem(
            title = "Read Receipts",
            subtitle = "Show when you've read messages",
            icon = R.drawable.ic_done_all,
            checked = readReceiptsEnabled,
            onCheckedChange = onReadReceiptsChanged,
            enabled = !isLoading
        )
        SettingsDivider()
        SettingsNavigationItem(
            title = "Disappearing Messages",
            subtitle = "Set default timer for new chats",
            icon = R.drawable.ic_timer,
            onClick = { },
            enabled = !isLoading
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupPrivacySection(isLoading: Boolean) {
    SettingsSection(title = "Group Privacy") {
        SettingsSelectionItem(
            title = "Groups",
            subtitle = "Control who can add you to groups",
            icon = R.drawable.ic_group,
            options = listOf("Everyone", "My Contacts", "My Contacts Except...", "Nobody"),
            selectedOption = "My Contacts",
            onSelect = { },
            enabled = !isLoading
        )
    }
}

@Composable
private fun SecuritySection(
    appLockEnabled: Boolean,
    onAppLockChanged: (Boolean) -> Unit,
    chatLockEnabled: Boolean,
    onChatLockChanged: (Boolean) -> Unit,
    isLoading: Boolean
) {
    SettingsSection(title = "Security") {
        SettingsToggleItem(
            title = "App Lock",
            subtitle = "Require authentication to open app",
            icon = R.drawable.ic_lock,
            checked = appLockEnabled,
            onCheckedChange = onAppLockChanged,
            enabled = !isLoading
        )
        SettingsDivider()
        SettingsToggleItem(
            title = "Chat Lock",
            subtitle = "Lock individual chats with authentication",
            icon = R.drawable.ic_chat_lock,
            checked = chatLockEnabled,
            onCheckedChange = onChatLockChanged,
            enabled = !isLoading
        )
    }
}

@Composable
private fun BlockedContactsSection(
    onNavigateToBlockedUsers: () -> Unit,
    isLoading: Boolean
) {
    SettingsSection(title = "Blocked Contacts") {
        SettingsNavigationItem(
            title = "Blocked Contacts",
            subtitle = "Manage blocked users",
            icon = R.drawable.ic_block,
            onClick = onNavigateToBlockedUsers,
            enabled = !isLoading
        )
    }
}
