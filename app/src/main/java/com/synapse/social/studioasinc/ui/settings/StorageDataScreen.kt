package com.synapse.social.studioasinc.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.synapse.social.studioasinc.R

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
            MediumTopAppBar(
                title = { Text("Storage and data") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Section 1: Manage Storage & Network Usage
            item {
                ListItem(
                    headlineContent = { Text("Manage storage") },
                    supportingContent = { Text("2.4 GB") }, // Mocked for main screen, real data in detailed screen
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.file_save_24px),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.clickable { navController?.navigate("settings_storage_manage") }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Network usage") },
                    supportingContent = { Text("1.8 GB sent • 2.1 GB received") },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_network_check),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.clickable { navController?.navigate("settings_network_usage") }
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Section 2: Use Less Data for Calls
            item {
                ListItem(
                    headlineContent = { Text("Use less data for calls") },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_call),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = useLessDataCalls,
                            onCheckedChange = { viewModel.setUseLessDataCalls(it) }
                        )
                    },
                    modifier = Modifier.clickable { viewModel.setUseLessDataCalls(!useLessDataCalls) }
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Section 3: Proxy
            item {
                ListItem(
                    headlineContent = { Text("Proxy") },
                    supportingContent = { Text("Off") },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_vpn_key),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.clickable { /* Placeholder */ }
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Section 4: Media Auto-Download
            item {
                Text(
                    text = "Media auto-download",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = "Voice messages are always automatically downloaded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("When using mobile data") },
                    supportingContent = { Text(getAutoDownloadSummary(autoDownloadRules.mobileData)) },
                    modifier = Modifier.clickable { showMobileDialog = true }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("When connected on Wi-Fi") },
                    supportingContent = { Text(getAutoDownloadSummary(autoDownloadRules.wifi)) },
                    modifier = Modifier.clickable { showWifiDialog = true }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("When roaming") },
                    supportingContent = { Text(getAutoDownloadSummary(autoDownloadRules.roaming)) },
                    modifier = Modifier.clickable { showRoamingDialog = true }
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Section 5: Media Upload Quality
            item {
                Text(
                    text = "Media upload quality",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = "Choose the quality of media files to be sent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Photo upload quality") },
                    supportingContent = { Text(mediaUploadQuality.displayName()) },
                    modifier = Modifier.clickable { viewModel.openMediaQualitySheet() }
                )
            }
        }
    }

    // Media Quality Bottom Sheet
    if (showMediaQualitySheet) {
        ModalBottomSheet(onDismissRequest = { viewModel.closeMediaQualitySheet() }) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = "Photo upload quality",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                MediaUploadQuality.values().forEach { quality ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setMediaUploadQuality(quality) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mediaUploadQuality == quality,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = quality.displayName(), style = MaterialTheme.typography.bodyLarge)
                            Text(text = quality.description(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
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

@Composable
fun AutoDownloadDialog(
    title: String,
    selectedTypes: Set<MediaType>,
    onConfirm: (Set<MediaType>) -> Unit,
    onDismiss: () -> Unit
) {
    val tempSelection = remember { mutableStateListOf(*selectedTypes.toTypedArray()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                MediaType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (tempSelection.contains(type)) {
                                    tempSelection.remove(type)
                                } else {
                                    tempSelection.add(type)
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = tempSelection.contains(type),
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(type.displayName())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(tempSelection.toSet()) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getAutoDownloadSummary(selectedTypes: Set<MediaType>): String {
    if (selectedTypes.isEmpty()) return "No media"
    if (selectedTypes.size == MediaType.values().size) return "All media"
    return selectedTypes.joinToString(", ") { it.displayName() }
}
