package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStorageScreen(
    viewModel: ManageStorageViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val storageUsage by viewModel.storageUsage.collectAsState()
    val chatList by viewModel.chatStorageList.collectAsState()
    val largeFiles by viewModel.largeFiles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Storage") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        if (isLoading || storageUsage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Storage Usage Bar Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val usage = storageUsage!!
                        val totalGB = formatBytesToGB(usage.totalSize)
                        val usedGB = formatBytesToGB(usage.usedSize)
                        val freeGB = formatBytesToGB(usage.freeSize)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$usedGB used",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$freeGB free",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Visual Storage Bar
                        StorageBar(usage = usage)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Legend
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synapse media", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.width(16.dp))

                            Badge(color = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Apps and other items", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceContainerLowest)
                }

                // Review and Delete Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Review and delete items",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                            ListItem(
                            headlineContent = { Text("Larger than 5 MB") },
                            supportingContent = { Text(formatBytes(largeFiles.sumOf { it.size })) },
                            leadingContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_document),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = {
                                Icon(painter = painterResource(id = R.drawable.ic_chevron_right), contentDescription = null)
                            },
                             modifier = Modifier.padding(vertical = 4.dp)
                         )
                     }

                    HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceContainerLowest)
                }

                // Chats Section
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Chats",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(chatList) { chat ->
                    ListItem(
                        headlineContent = { Text(chat.chatName) },
                        supportingContent = { Text(formatBytes(chat.size)) },
                        leadingContent = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = chat.chatName.first().toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        },
                        modifier = Modifier.clickable { /* Navigate to chat details */ }
                    )
                }
            }
        }
    }
}

@Composable
fun StorageBar(usage: StorageUsageBreakdown) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(MaterialTheme.shapes.small)
    ) {
        val totalWidth = size.width
        val synapseWidth = (usage.synapseSize.toFloat() / usage.totalSize) * totalWidth
        val otherWidth = (usage.appsAndOtherSize.toFloat() / usage.totalSize) * totalWidth

        // Draw background (Free)
        drawRect(color = Color.LightGray.copy(alpha = 0.3f))

        // Draw Synapse usage
        drawLine(
            color = Color(0xFF6750A4), // Primary
            start = Offset(0f, size.height / 2),
            end = Offset(synapseWidth, size.height / 2),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )

        // Draw Apps/Other usage
        drawLine(
            color = Color(0xFF7D5260), // Tertiary
            start = Offset(synapseWidth, size.height / 2),
            end = Offset(synapseWidth + otherWidth, size.height / 2),
            strokeWidth = size.height,
            cap = StrokeCap.Butt
        )
    }
}

@Composable
fun Badge(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape)
    )
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

private fun formatBytesToGB(bytes: Long): String {
    return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
}
