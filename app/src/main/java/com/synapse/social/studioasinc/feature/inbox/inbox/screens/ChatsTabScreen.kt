package com.synapse.social.studioasinc.ui.inbox.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.synapse.social.studioasinc.ui.inbox.components.InboxEmptyState
import com.synapse.social.studioasinc.ui.inbox.models.EmptyStateType

@Composable
fun ChatsTabScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    InboxEmptyState(
        type = EmptyStateType.CHATS,
        message = "Chat functionality is currently unavailable",
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
    )
}
