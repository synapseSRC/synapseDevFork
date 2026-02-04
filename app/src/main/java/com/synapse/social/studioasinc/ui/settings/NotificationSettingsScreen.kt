package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

private val SocialNotificationTypes = listOf(
    NotificationCategory.LIKES to "Likes",
    NotificationCategory.COMMENTS to "Comments",
    NotificationCategory.REPLIES to "Replies",
    NotificationCategory.FOLLOWS to "New Followers",
    NotificationCategory.MENTIONS to "Mentions"
)

private val ContentNotificationTypes = listOf(
    NotificationCategory.NEW_POSTS to "New Posts from Followed Users",
    NotificationCategory.SHARES to "Shares of Your Posts"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel,
    onBackClick: () -> Unit
) {
    val notificationPreferences by viewModel.notificationPreferences.collectAsState()
    val error by viewModel.error.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showVibrateSheet by remember { mutableStateOf(false) }
    var vibrateMode by remember { mutableStateOf("Default") }

    var showLedSheet by remember { mutableStateOf(false) }
    var ledColor by remember { mutableStateOf("Blue") }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val startTimeState = key(notificationPreferences.quietHoursStart) {
        rememberTimePickerState(
            initialHour = notificationPreferences.quietHoursStart.split(":").getOrNull(0)?.toIntOrNull() ?: 22,
            initialMinute = notificationPreferences.quietHoursStart.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
            is24Hour = true
        )
    }

    val endTimeState = key(notificationPreferences.quietHoursEnd) {
        rememberTimePickerState(
            initialHour = notificationPreferences.quietHoursEnd.split(":").getOrNull(0)?.toIntOrNull() ?: 8,
            initialMinute = notificationPreferences.quietHoursEnd.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
            is24Hour = true
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = {
            error?.let { errorMessage ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(errorMessage)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Global Controls
            item {
                NotifSettingsSection(title = "Global Settings") {
                    NotifSettingsCard {
                        NotifSettingsRow(
                            icon = Icons.Default.Notifications,
                            title = "Enable Notifications",
                            subtitle = if (notificationPreferences.globalEnabled) "On" else "Off",
                            trailingContent = {
                                Switch(
                                    checked = notificationPreferences.globalEnabled,
                                    onCheckedChange = { viewModel.toggleGlobalNotifications(it) }
                                )
                            },
                            onClick = { viewModel.toggleGlobalNotifications(!notificationPreferences.globalEnabled) }
                        )
                    }
                }
            }

            // Section 2: Social Interactions
            item {
                NotifSettingsSection(title = "Social Interactions") {
                    NotifSettingsCard {
                        SocialNotificationTypes.forEachIndexed { index, (category, label) ->
                            val isEnabled = notificationPreferences.isEnabled(category)
                            NotifSettingsRow(
                                icon = when(category) {
                                    NotificationCategory.LIKES -> Icons.Default.EmojiEmotions
                                    NotificationCategory.FOLLOWS -> Icons.Default.Group
                                    else -> Icons.Default.Notifications
                                },
                                title = label,
                                subtitle = if (isEnabled) "Enabled" else "Disabled",
                                trailingContent = {
                                    Switch(
                                        checked = isEnabled,
                                        onCheckedChange = { viewModel.toggleNotificationCategory(category, it) },
                                        enabled = notificationPreferences.globalEnabled
                                    )
                                },
                                onClick = {
                                    if (notificationPreferences.globalEnabled) {
                                        viewModel.toggleNotificationCategory(category, !isEnabled)
                                    }
                                }
                            )
                            if (index < SocialNotificationTypes.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // Section 3: Content Updates
            item {
                NotifSettingsSection(title = "Content Updates") {
                    NotifSettingsCard {
                        ContentNotificationTypes.forEachIndexed { index, (category, label) ->
                            val isEnabled = notificationPreferences.isEnabled(category)
                            NotifSettingsRow(
                                icon = Icons.Default.ContentCopy,
                                title = label,
                                subtitle = if (isEnabled) "Enabled" else "Disabled",
                                trailingContent = {
                                    Switch(
                                        checked = isEnabled,
                                        onCheckedChange = { viewModel.toggleNotificationCategory(category, it) },
                                        enabled = notificationPreferences.globalEnabled
                                    )
                                },
                                onClick = {
                                    if (notificationPreferences.globalEnabled) {
                                        viewModel.toggleNotificationCategory(category, !isEnabled)
                                    }
                                }
                            )
                            if (index < ContentNotificationTypes.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // Section 4: System & Security
            item {
                NotifSettingsSection(title = "System & Security") {
                    NotifSettingsCard {
                        NotifSettingsRow(
                            icon = Icons.Default.Info,
                            title = "Security Alerts",
                            subtitle = "Always enabled",
                            trailingContent = {
                                Switch(
                                    checked = true,
                                    onCheckedChange = { },
                                    enabled = false
                                )
                            },
                            onClick = { }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        NotifSettingsRow(
                            icon = Icons.Default.Settings,
                            title = "App Updates",
                            subtitle = if (notificationPreferences.updatesEnabled) "Enabled" else "Disabled",
                            trailingContent = {
                                Switch(
                                    checked = notificationPreferences.updatesEnabled,
                                    onCheckedChange = { viewModel.toggleNotificationCategory(NotificationCategory.SYSTEM_UPDATES, it) },
                                    enabled = notificationPreferences.globalEnabled
                                )
                            },
                            onClick = {
                                if (notificationPreferences.globalEnabled) {
                                    viewModel.toggleNotificationCategory(NotificationCategory.SYSTEM_UPDATES, !notificationPreferences.updatesEnabled)
                                }
                            }
                        )
                    }
                }
            }

            // Section 5: Advanced
            item {
                NotifSettingsSection(title = "Advanced Settings") {
                    NotifSettingsCard {
                         NotifSettingsRow(
                            icon = Icons.Default.Schedule,
                            title = "Quiet Hours",
                            subtitle = if (notificationPreferences.quietHoursEnabled)
                                "${notificationPreferences.quietHoursStart} - ${notificationPreferences.quietHoursEnd}"
                                else "Disabled",
                            trailingContent = {
                                Switch(
                                    checked = notificationPreferences.quietHoursEnabled,
                                    onCheckedChange = { viewModel.toggleQuietHours(it) },
                                    enabled = notificationPreferences.globalEnabled
                                )
                            },
                            onClick = {
                                if (notificationPreferences.globalEnabled) {
                                    showStartTimePicker = true
                                }
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        NotifSettingsRow(
                            icon = Icons.Default.DoNotDisturb,
                            title = "Do Not Disturb",
                            subtitle = if (notificationPreferences.doNotDisturb) "Active" else "Inactive",
                            trailingContent = {
                                Switch(
                                    checked = notificationPreferences.doNotDisturb,
                                    onCheckedChange = { viewModel.toggleDoNotDisturb(it) },
                                    enabled = notificationPreferences.globalEnabled
                                )
                            },
                            onClick = {
                                if (notificationPreferences.globalEnabled) {
                                    viewModel.toggleDoNotDisturb(!notificationPreferences.doNotDisturb)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showStartTimePicker) {
        Dialog(onDismissRequest = { showStartTimePicker = false }) {
            Card {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Start Quiet Hours", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = startTimeState)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            val time = "${startTimeState.hour.toString().padStart(2, '0')}:${startTimeState.minute.toString().padStart(2, '0')}"
                            viewModel.setQuietHours(time, notificationPreferences.quietHoursEnd)
                            showStartTimePicker = false
                            showEndTimePicker = true
                        }) { Text("Next") }
                    }
                }
            }
        }
    }

    if (showEndTimePicker) {
        Dialog(onDismissRequest = { showEndTimePicker = false }) {
            Card {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("End Quiet Hours", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = endTimeState)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            val time = "${endTimeState.hour.toString().padStart(2, '0')}:${endTimeState.minute.toString().padStart(2, '0')}"
                            viewModel.setQuietHours(notificationPreferences.quietHoursStart, time)
                            showEndTimePicker = false
                        }) { Text("Done") }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotifSettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun NotifSettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun NotifSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
