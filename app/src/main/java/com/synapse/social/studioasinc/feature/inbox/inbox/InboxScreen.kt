package com.synapse.social.studioasinc.ui.inbox

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synapse.social.studioasinc.ui.inbox.models.InboxUiState
import com.synapse.social.studioasinc.ui.inbox.components.InboxTopAppBar
import com.synapse.social.studioasinc.ui.inbox.screens.CallsTabScreen
import com.synapse.social.studioasinc.ui.inbox.screens.ChatsTabScreen
import com.synapse.social.studioasinc.ui.inbox.screens.ContactsTabScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String, String) -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: InboxViewModel = viewModel(factory = InboxViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            InboxTopAppBar(
                title = "Inbox",
                avatarUrl = currentUserProfile?.avatar,
                scrollBehavior = scrollBehavior,
                selectionMode = false,
                selectedCount = 0,
                onSearchClick = { /* No-op */ },
                onSelectionClose = { /* No-op */ },
                onDeleteSelected = { /* No-op */ },
                onArchiveSelected = { /* No-op */ },
                onProfileClick = { currentUserProfile?.id?.let(onNavigateToProfile) }
            )
        },
        bottomBar = {
             NavigationBar(
                 containerColor = MaterialTheme.colorScheme.surfaceContainer,
                 contentColor = MaterialTheme.colorScheme.onSurface
             ) {
                 val tabs = listOf("Messages", "Calls", "Contacts")
                 tabs.forEachIndexed { index, title ->
                     val selected = pagerState.currentPage == index
                     NavigationBarItem(
                         selected = selected,
                         onClick = {
                             scope.launch { pagerState.animateScrollToPage(index) }
                         },
                         label = {
                             Text(
                                 text = title,
                                 color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                             )
                         },
                         icon = {
                             Icon(
                                 imageVector = when (index) {
                                     0 -> if (selected) Icons.Filled.Email else Icons.Outlined.Email
                                     1 -> if (selected) Icons.Filled.Call else Icons.Outlined.Call
                                     else -> if (selected) Icons.Filled.Group else Icons.Outlined.Group
                                 },
                                 contentDescription = title,
                                 tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                             )
                         },
                         colors = NavigationBarItemDefaults.colors(
                             selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                             selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                             indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                             unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                             unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                         )
                     )
                 }
             }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) { paddingValues ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> ChatsTabScreen()
                1 -> CallsTabScreen()
                2 -> ContactsTabScreen()
            }
        }
    }
}
