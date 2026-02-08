package com.synapse.social.studioasinc.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.synapse.social.studioasinc.R
import com.synapse.social.studioasinc.feature.shared.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageDataScreen(
    viewModel: StorageDataViewModel,
    onBackClick: () -> Unit,
    navController: NavController? = null
) {
    val mediaUploadQuality by viewModel.mediaUploadQuality.collectAsState()
    val autoDownloadRules by viewModel.autoDownloadRules.collectAsState()
    val useLessDataCalls by viewModel.useLessDataCalls.collectAsState()
    val showMediaQualitySheet by viewModel.showMediaQualitySheet.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // State for Auto-Download Dialogs
    var showMobileDialog by remember { mutableStateOf(false) }
    var showWifiDialog by remember { mutableStateOf(false) }
    var showRoamingDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Storage and data") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = SettingsSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(SettingsSpacing.sectionSpacing),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            // Section 1: Storage Management
            item {
                SettingsSection(title = "Storage Management") {
                    SettingsNavigationItem(
                        title = "Manage storage",
                        subtitle = "2.4 GB", // Mocked as per original
                        icon = R.drawable.file_save_24px,
                        onClick = { navController?.navigate("settings_storage_manage") },
                        position = SettingsItemPosition.Top
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        title = "Network usage",
                        subtitle = "1.8 GB sent • 2.1 GB received",
                        icon = R.drawable.ic_network_check,
                        onClick = { navController?.navigate("settings_network_usage") },
                        position = SettingsItemPosition.Bottom
                    )
                }
            }

            // Section 2: Call Settings
            item {
                SettingsSection(title = "Call Settings") {
                    SettingsToggleItem(
                        title = "Use less data for calls",
                        icon = R.drawable.ic_call,
                        checked = useLessDataCalls,
                        onCheckedChange = { viewModel.setUseLessDataCalls(it) },
                        position = SettingsItemPosition.Single
                    )
                }
            }

            // Section 3: Network
            item {
                SettingsSection(title = "Network") {
                    SettingsNavigationItem(
                        title = "Proxy",
                        subtitle = "Off",
                        icon = R.drawable.ic_vpn_key,
                        onClick = { /* Placeholder */ },
                        position = SettingsItemPosition.Single
                    )
                }
            }

            // Section 4: Media auto-download
            item {
                SettingsSection(title = "Media auto-download") {
                    Text(
                        text = "Voice messages are always automatically downloaded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            start = SettingsSpacing.itemHorizontalPadding,
                            end = SettingsSpacing.itemHorizontalPadding,
                            top = SettingsSpacing.itemVerticalPadding,
                            bottom = Spacing.ExtraSmall
                        )
                    )
                    SettingsNavigationItem(
                        title = "When using mobile data",
                        subtitle = getAutoDownloadSummary(autoDownloadRules.mobileData),
                        onClick = { showMobileDialog = true },
                        position = SettingsItemPosition.Top
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        title = "When connected on Wi-Fi",
                        subtitle = getAutoDownloadSummary(autoDownloadRules.wifi),
                        onClick = { showWifiDialog = true },
                        position = SettingsItemPosition.Middle
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        title = "When roaming",
                        subtitle = getAutoDownloadSummary(autoDownloadRules.roaming),
                        onClick = { showRoamingDialog = true },
                        position = SettingsItemPosition.Bottom
                    )
                }
            }

            // Section 5: Media upload quality
            item {
                SettingsSection(title = "Media upload quality") {
                    Text(
                        text = "Choose the quality of media files to be sent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            start = SettingsSpacing.itemHorizontalPadding,
                            end = SettingsSpacing.itemHorizontalPadding,
                            top = SettingsSpacing.itemVerticalPadding,
                            bottom = Spacing.ExtraSmall
                        )
                    )
                    SettingsClickableItem(
                        title = "Photo upload quality",
                        subtitle = mediaUploadQuality.displayName(),
                        onClick = { viewModel.openMediaQualitySheet() },
                        position = SettingsItemPosition.Single
                    )
                }
            }

            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Media Quality Bottom Sheet
    if (showMediaQualitySheet) {
        MediaQualityBottomSheet(
            onDismissRequest = { viewModel.closeMediaQualitySheet() },
            currentQuality = mediaUploadQuality,
            onQualitySelected = { viewModel.setMediaUploadQuality(it) }
        )
    }

    // Auto-Download Dialogs
    if (showMobileDialog) {
        AutoDownloadDialog(
            title = "When using mobile data",
            selectedTypes = autoDownloadRules.mobileData,
            onConfirm = {
                viewModel.setAutoDownloadRule("mobile", it)
                showMobileDialog = false
            },
            onDismiss = { showMobileDialog = false }
        )
    }

    if (showWifiDialog) {
        AutoDownloadDialog(
            title = "When connected on Wi-Fi",
            selectedTypes = autoDownloadRules.wifi,
            onConfirm = {
                viewModel.setAutoDownloadRule("wifi", it)
                showWifiDialog = false
            },
            onDismiss = { showWifiDialog = false }
        )
    }

    if (showRoamingDialog) {
        AutoDownloadDialog(
            title = "When roaming",
            selectedTypes = autoDownloadRules.roaming,
            onConfirm = {
                viewModel.setAutoDownloadRule("roaming", it)
                showRoamingDialog = false
            },
            onDismiss = { showRoamingDialog = false }
        )
    }
}

private fun getAutoDownloadSummary(selectedTypes: Set<MediaType>): String {
    if (selectedTypes.isEmpty()) return "No media"
    if (selectedTypes.size == MediaType.values().size) return "All media"
    return selectedTypes.joinToString(", ") { it.displayName() }
}
