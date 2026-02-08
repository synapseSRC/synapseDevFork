package com.synapse.social.studioasinc.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaQualityBottomSheet(
    onDismissRequest: () -> Unit,
    currentQuality: MediaUploadQuality,
    onQualitySelected: (MediaUploadQuality) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
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
                        .clickable { onQualitySelected(quality) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentQuality == quality,
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

@Composable
fun AutoDownloadDialog(
    title: String,
    selectedTypes: Set<MediaType>,
    onConfirm: (Set<MediaType>) -> Unit,
    onDismiss: () -> Unit
) {
    val tempSelection = remember(selectedTypes) { mutableStateListOf(*selectedTypes.toTypedArray()) }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageDataScreenDialogs(
    showMediaQualitySheet: Boolean,
    mediaUploadQuality: MediaUploadQuality,
    autoDownloadRules: AutoDownloadRules,
    showMobileDialog: Boolean,
    showWifiDialog: Boolean,
    showRoamingDialog: Boolean,
    onCloseMediaQualitySheet: () -> Unit,
    onSetMediaUploadQuality: (MediaUploadQuality) -> Unit,
    onSetAutoDownloadRule: (String, Set<MediaType>) -> Unit,
    onDismissMobileDialog: () -> Unit,
    onDismissWifiDialog: () -> Unit,
    onDismissRoamingDialog: () -> Unit
) {
    // Media Quality Bottom Sheet
    if (showMediaQualitySheet) {
        MediaQualityBottomSheet(
            onDismissRequest = onCloseMediaQualitySheet,
            currentQuality = mediaUploadQuality,
            onQualitySelected = onSetMediaUploadQuality
        )
    }

    // Auto-Download Dialogs
    if (showMobileDialog) {
        AutoDownloadDialog(
            title = "When using mobile data",
            selectedTypes = autoDownloadRules.mobileData,
            onConfirm = {
                onSetAutoDownloadRule("mobile", it)
                onDismissMobileDialog()
            },
            onDismiss = onDismissMobileDialog
        )
    }

    if (showWifiDialog) {
        AutoDownloadDialog(
            title = "When connected on Wi-Fi",
            selectedTypes = autoDownloadRules.wifi,
            onConfirm = {
                onSetAutoDownloadRule("wifi", it)
                onDismissWifiDialog()
            },
            onDismiss = onDismissWifiDialog
        )
    }

    if (showRoamingDialog) {
        AutoDownloadDialog(
            title = "When roaming",
            selectedTypes = autoDownloadRules.roaming,
            onConfirm = {
                onSetAutoDownloadRule("roaming", it)
                onDismissRoamingDialog()
            },
            onDismiss = onDismissRoamingDialog
        )
    }
}
