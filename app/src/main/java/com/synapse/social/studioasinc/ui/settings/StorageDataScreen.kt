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

    // State for Auto-Download Dialogs
    var showMobileDialog by remember { mutableStateOf(false) }
    var showWifiDialog by remember { mutableStateOf(false) }
    var showRoamingDialog by remember { mutableStateOf(false) }

    StorageDataContent(
        mediaUploadQuality = mediaUploadQuality,
        autoDownloadRules = autoDownloadRules,
        useLessDataCalls = useLessDataCalls,
        onBackClick = onBackClick,
        onNavigateToStorageManage = { navController?.navigate("settings_storage_manage") },
        onNavigateToNetworkUsage = { navController?.navigate("settings_network_usage") },
        onNavigateToProxy = { /* Placeholder */ },
        onUseLessDataCallsChanged = { viewModel.setUseLessDataCalls(it) },
        onOpenMobileDialog = { showMobileDialog = true },
        onOpenWifiDialog = { showWifiDialog = true },
        onOpenRoamingDialog = { showRoamingDialog = true },
        onOpenMediaQualitySheet = { viewModel.openMediaQualitySheet() }
    )

    StorageDataScreenDialogs(
        showMediaQualitySheet = showMediaQualitySheet,
        mediaUploadQuality = mediaUploadQuality,
        autoDownloadRules = autoDownloadRules,
        showMobileDialog = showMobileDialog,
        showWifiDialog = showWifiDialog,
        showRoamingDialog = showRoamingDialog,
        onCloseMediaQualitySheet = { viewModel.closeMediaQualitySheet() },
        onSetMediaUploadQuality = { viewModel.setMediaUploadQuality(it) },
        onSetAutoDownloadRule = { type, rules -> viewModel.setAutoDownloadRule(type, rules) },
        onDismissMobileDialog = { showMobileDialog = false },
        onDismissWifiDialog = { showWifiDialog = false },
        onDismissRoamingDialog = { showRoamingDialog = false }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StorageDataContent(
    mediaUploadQuality: MediaUploadQuality,
    autoDownloadRules: AutoDownloadRules,
    useLessDataCalls: Boolean,
    onBackClick: () -> Unit,
    onNavigateToStorageManage: () -> Unit,
    onNavigateToNetworkUsage: () -> Unit,
    onNavigateToProxy: () -> Unit,
    onUseLessDataCallsChanged: (Boolean) -> Unit,
    onOpenMobileDialog: () -> Unit,
    onOpenWifiDialog: () -> Unit,
    onOpenRoamingDialog: () -> Unit,
    onOpenMediaQualitySheet: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
                StorageManagementSection(
                    onNavigateToStorageManage = onNavigateToStorageManage,
                    onNavigateToNetworkUsage = onNavigateToNetworkUsage
                )
            }

            // Section 2: Call Settings
            item {
                CallSettingsSection(
                    useLessDataCalls = useLessDataCalls,
                    onUseLessDataCallsChanged = onUseLessDataCallsChanged
                )
            }

            // Section 3: Network
            item {
                NetworkSection(
                    onNavigateToProxy = onNavigateToProxy
                )
            }

            // Section 4: Media auto-download
            item {
                MediaAutoDownloadSection(
                    autoDownloadRules = autoDownloadRules,
                    onOpenMobileDialog = onOpenMobileDialog,
                    onOpenWifiDialog = onOpenWifiDialog,
                    onOpenRoamingDialog = onOpenRoamingDialog
                )
            }

            // Section 5: Media upload quality
            item {
                MediaUploadQualitySection(
                    mediaUploadQuality = mediaUploadQuality,
                    onOpenMediaQualitySheet = onOpenMediaQualitySheet
                )
            }

            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
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
        SettingsDivider()
        SettingsNavigationItem(
            title = "Network usage",
            subtitle = "1.8 GB sent • 2.1 GB received",
            icon = R.drawable.ic_network_check,
            onClick = onNavigateToNetworkUsage,
            position = SettingsItemPosition.Bottom
        )
    }
}

private fun getAutoDownloadSummary(selectedTypes: Set<MediaType>): String {
    if (selectedTypes.isEmpty()) return "No media"
    if (selectedTypes.size == MediaType.values().size) return "All media"
    return selectedTypes.joinToString(", ") { it.displayName() }
}
