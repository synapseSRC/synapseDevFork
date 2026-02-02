package com.synapse.social.studioasinc.ui.profile.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.synapse.social.studioasinc.core.util.NumberFormatter

/**
 * Enhanced stat item component with better visual hierarchy and accessibility.
 * Displays a number prominently with a label below, includes proper touch targets
 * and haptic feedback for better user experience.
 */
@Composable
fun StatItem(
    count: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "statItemScale"
    )
    
    val formattedCount = NumberFormatter.formatCount(count)
    val contentDesc = "$formattedCount $label"
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .minimumInteractiveComponentSize()
            .scale(scale)
            .semantics {
                contentDescription = contentDesc
            }
    ) {
        Text(
            text = NumberFormatter.formatCount(count),
            style = MaterialTheme.typography.titleLarge, // MD3: Larger title
            fontWeight = FontWeight.Bold,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            },
            textAlign = TextAlign.Center
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            },
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Row of stat items with consistent spacing and alignment
 */
@Composable
fun StatsRow(
    postsCount: Int,
    followersCount: Int,
    followingCount: Int,
    onStatsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        StatItem(
            count = postsCount,
            label = "posts",
            onClick = { onStatsClick("posts") }
        )
        
        StatItem(
            count = followersCount,
            label = "followers",
            onClick = { onStatsClick("followers") }
        )
        
        StatItem(
            count = followingCount,
            label = "following",
            onClick = { onStatsClick("following") }
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun StatItemPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatItem(
                count = 1234,
                label = "followers",
                onClick = { }
            )
            
            StatsRow(
                postsCount = 42,
                followersCount = 1234,
                followingCount = 567,
                onStatsClick = { }
            )
        }
    }
}
