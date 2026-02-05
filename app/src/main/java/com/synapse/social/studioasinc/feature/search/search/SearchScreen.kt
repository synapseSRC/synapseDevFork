package com.synapse.social.studioasinc.ui.search

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.synapse.social.studioasinc.R
import com.synapse.social.studioasinc.feature.search.search.components.AccountCard
import com.synapse.social.studioasinc.feature.search.search.components.HashtagCard
import com.synapse.social.studioasinc.feature.search.search.components.NewsCard
import com.synapse.social.studioasinc.feature.search.search.components.PostCard
import com.synapse.social.studioasinc.ui.components.ExpressiveLoadingIndicator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToPost: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Sync Pager with Tab
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { SearchTab.entries.size }
    )

    LaunchedEffect(uiState.selectedTab) {
        pagerState.animateScrollToPage(uiState.selectedTab.ordinal)
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelected(SearchTab.entries[pagerState.currentPage])
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Mastodon-style Search Bar
            SearchBar(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = viewModel::onSearch,
                active = false, // Always expanded in this design, or customize
                onActiveChange = {},
                placeholder = { Text("Search Synapse") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                     if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearSearch) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    } else {
                        IconButton(onClick = { /* TODO: Implement QR Scan */ }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                        }
                    }
                },
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    dividerColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {}
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
            ) {
                SearchTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val tab = SearchTab.entries[page]

                Box(modifier = Modifier.fillMaxSize()) {
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            ExpressiveLoadingIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            when (tab) {
                                SearchTab.POSTS -> {
                                    if (uiState.posts.isEmpty()) {
                                        item { EmptyState("No posts found") }
                                    } else {
                                        items(uiState.posts, key = { it.id }) { post ->
                                            PostCard(
                                                post = post,
                                                onClick = { onNavigateToPost(post.id) }
                                            )
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                thickness = 0.5.dp
                                            )
                                        }
                                    }
                                }
                                SearchTab.HASHTAGS -> {
                                     if (uiState.hashtags.isEmpty()) {
                                        item { EmptyState("No hashtags found") }
                                    } else {
                                        items(uiState.hashtags, key = { it.id }) { hashtag ->
                                            HashtagCard(
                                                hashtag = hashtag,
                                                onClick = { viewModel.onSearch(hashtag.tag) }
                                            )
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                thickness = 0.5.dp
                                            )
                                        }
                                    }
                                }
                                SearchTab.NEWS -> {
                                     if (uiState.news.isEmpty()) {
                                        item { EmptyState("No news found") }
                                    } else {
                                        items(uiState.news, key = { it.id }) { news ->
                                            NewsCard(
                                                news = news,
                                                onClick = {
                                                    news.url?.let { url ->
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                        context.startActivity(intent)
                                                    }
                                                }
                                            )
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                thickness = 0.5.dp
                                            )
                                        }
                                    }
                                }
                                SearchTab.FOR_YOU -> {
                                     if (uiState.accounts.isEmpty()) {
                                        item { EmptyState("No accounts found") }
                                    } else {
                                        items(uiState.accounts, key = { it.id }) { account ->
                                            AccountCard(
                                                account = account,
                                                onClick = { onNavigateToProfile(account.id) },
                                                onFollowClick = { /* TODO: Implement Follow */ }
                                            )
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                thickness = 0.5.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
