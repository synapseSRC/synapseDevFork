package com.synapse.social.studioasinc.feature.profile.editprofile.components.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.synapse.social.studioasinc.shared.domain.model.PrivacyLevel

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector? = null,
    privacyLevel: PrivacyLevel,
    onEditClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Privacy Indicator
                    IconButton(onClick = onPrivacyClick) {
                        // Icon based on privacy level (Placeholder icons)
                        // val privacyIcon = when (privacyLevel) { ... }
                        // Icon(privacyIcon, ...)
                        Text(text = privacyLevel.name.take(1)) // Temp placeholder
                    }

                    // Edit Button (if whole section is editable via one dialog)
                    IconButton(onClick = onEditClick) {
                        // Icon(Icons.Default.Edit, ...)
                        Text("Edit") // Temp
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}
