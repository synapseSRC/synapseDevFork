package com.synapse.social.studioasinc.ui.settings

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.synapse.social.studioasinc.ui.settings.about.AboutSupportScreen
import com.synapse.social.studioasinc.ui.settings.about.AboutSupportViewModel
import com.synapse.social.studioasinc.ui.settings.account.RequestAccountInfoScreen
import com.synapse.social.studioasinc.ui.settings.account.RequestAccountInfoViewModel
import com.synapse.social.studioasinc.ui.settings.appearance.AppearanceScreen
import com.synapse.social.studioasinc.ui.settings.appearance.AppearanceViewModel
import com.synapse.social.studioasinc.ui.settings.data.NetworkUsageScreen
import com.synapse.social.studioasinc.ui.settings.data.NetworkUsageViewModel
import com.synapse.social.studioasinc.ui.settings.language.LanguageRegionScreen
import com.synapse.social.studioasinc.ui.settings.language.LanguageRegionViewModel
import com.synapse.social.studioasinc.ui.settings.notifications.NotificationSettingsScreen
import com.synapse.social.studioasinc.ui.settings.notifications.NotificationSettingsViewModel
import com.synapse.social.studioasinc.ui.settings.privacy.PrivacySecurityScreen
import com.synapse.social.studioasinc.ui.settings.privacy.PrivacySecurityViewModel
import com.synapse.social.studioasinc.ui.settings.storage.ManageStorageScreen
import com.synapse.social.studioasinc.ui.settings.storage.ManageStorageViewModel
import com.synapse.social.studioasinc.ui.settings.storage.StorageDataScreen
import com.synapse.social.studioasinc.ui.settings.storage.StorageDataViewModel
import com.synapse.social.studioasinc.ui.settings.storage.StorageDataViewModelFactory
import com.synapse.social.studioasinc.ui.settings.licenses.LicensesScreen
import com.synapse.social.studioasinc.data.repository.SettingsRepository
import com.synapse.social.studioasinc.ui.settings.synapseplus.SynapsePlusScreen
import com.synapse.social.studioasinc.ui.settings.avatar.AvatarScreen
import com.synapse.social.studioasinc.ui.settings.favourites.FavouritesScreen
import com.synapse.social.studioasinc.ui.settings.accessibility.AccessibilityScreen
import com.synapse.social.studioasinc.ui.settings.apikey.ApiKeySettingsScreen
import com.synapse.social.studioasinc.ui.settings.apikey.ApiKeySettingsViewModel

/**
 * Navigation host for the Settings feature.
 *
 * Configures the navigation graph and handles transitions between settings screens.
 *
 * Requirements: 1.1
 */
@Composable
fun SettingsNavHost(
    navController: NavHostController,
    settingsRepository: SettingsRepository,
    onBackClick: () -> Unit,
    onNavigateToProfileEdit: () -> Unit,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = SettingsDestination.ROUTE_HUB,
        enterTransition = { SettingsAnimations.enterTransition },
        exitTransition = { SettingsAnimations.exitTransition },
        popEnterTransition = { SettingsAnimations.popEnterTransition },
        popExitTransition = { SettingsAnimations.popExitTransition }
    ) {
        // Settings Hub - Main screen with categorized settings
        composable(route = SettingsDestination.ROUTE_HUB) {
            val viewModel: SettingsHubViewModel = hiltViewModel()
            SettingsHubScreen(
                viewModel = viewModel,
                onBackClick = onBackClick,
                onNavigateToCategory = { destination ->
                    navController.navigate(destination.route)
                }
            )
        }

        // Account Settings Screen
        composable(route = SettingsDestination.ROUTE_ACCOUNT) {
            val viewModel: AccountSettingsViewModel = viewModel()
            AccountSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditProfile = onNavigateToProfileEdit,
                onLogout = onLogout,
                onNavigateToRequestAccountInfo = {
                    navController.navigate(SettingsDestination.ROUTE_REQUEST_ACCOUNT_INFO)
                },
                onNavigateToBusinessPlatform = {
                    navController.navigate(SettingsDestination.ROUTE_BUSINESS_PLATFORM)
                onNavigateToTwoFactorAuth = {
                    navController.navigate(SettingsDestination.ROUTE_TWO_FACTOR_AUTH)
                }
            )
        }

        // Two-Factor Authentication Screen
        composable(route = SettingsDestination.ROUTE_TWO_FACTOR_AUTH) {
            val viewModel: TwoFactorAuthViewModel = hiltViewModel()
            TwoFactorAuthScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Request Account Information Screen
        composable(route = SettingsDestination.ROUTE_REQUEST_ACCOUNT_INFO) {
            val viewModel: RequestAccountInfoViewModel = viewModel()
            RequestAccountInfoScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Privacy & Security Settings Screen
        composable(route = SettingsDestination.ROUTE_PRIVACY) {
            val viewModel: PrivacySecurityViewModel = viewModel()
            PrivacySecurityScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToBlockedUsers = {
                    // Placeholder - navigate to blocked users screen
                },
                onNavigateToMutedUsers = {
                    // Placeholder - navigate to muted users screen
                },
                onNavigateToActiveSessions = {
                    // Placeholder - navigate to active sessions screen
                }
            )
        }

        // Appearance Settings Screen
        composable(route = SettingsDestination.ROUTE_APPEARANCE) {
            val viewModel: AppearanceViewModel = viewModel()
            AppearanceScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChatCustomization = {
                    // Navigate to chat customization screen
                }
            )
        }

        // Notification Settings Screen
        composable(route = SettingsDestination.ROUTE_NOTIFICATIONS) {
            val viewModel: NotificationSettingsViewModel = hiltViewModel()
            NotificationSettingsScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = SettingsDestination.ROUTE_CHAT) {
            ChatPlaceholderScreen(
                title = "Chat Settings",
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = SettingsDestination.ROUTE_CHAT_THEME) {
            ChatPlaceholderScreen(
                title = "Chat Themes",
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = SettingsDestination.ROUTE_CHAT_WALLPAPER) {
            ChatPlaceholderScreen(
                title = "Chat Wallpapers",
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = SettingsDestination.ROUTE_CHAT_HISTORY_DELETION) {
            ChatPlaceholderScreen(
                title = "Chat History Deletion",
                onBackClick = { navController.popBackStack() }
            )
        }

        // Storage & Data Settings Screen
        composable(route = SettingsDestination.ROUTE_STORAGE) {
            val viewModel: StorageDataViewModel = viewModel(
                factory = StorageDataViewModelFactory(settingsRepository)
            )
            StorageDataScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                navController = navController
            )
        }

        // Manage Storage Screen
        composable(route = SettingsDestination.ROUTE_MANAGE_STORAGE) {
            val viewModel: ManageStorageViewModel = hiltViewModel()
            ManageStorageScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Network Usage Screen
        composable(route = SettingsDestination.ROUTE_NETWORK_USAGE) {
            val viewModel: NetworkUsageViewModel = hiltViewModel()
            NetworkUsageScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Business Platform Screen
        composable(route = SettingsDestination.ROUTE_BUSINESS_PLATFORM) {
            val viewModel: BusinessPlatformViewModel = hiltViewModel()
            BusinessPlatformScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Storage Provider Configuration Screen
        composable(route = SettingsDestination.ROUTE_STORAGE_PROVIDER) {
            val viewModel: SettingsViewModel = hiltViewModel()
            StorageProviderScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }

        // Language & Region Settings Screen
        composable(route = SettingsDestination.ROUTE_LANGUAGE) {
            val viewModel: LanguageRegionViewModel = viewModel()
            LanguageRegionScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // About & Support Settings Screen
        composable(route = SettingsDestination.ROUTE_ABOUT) {
            val viewModel: AboutSupportViewModel = viewModel()
            AboutSupportScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToLicenses = {
                    navController.navigate(SettingsDestination.ROUTE_LICENSES)
                },
                viewModel = viewModel
            )
        }

        // Open Source Licenses Screen
        composable(route = SettingsDestination.ROUTE_LICENSES) {
            LicensesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // API Key Settings Screen
        composable(route = SettingsDestination.ROUTE_API_KEY) {
            val viewModel: ApiKeySettingsViewModel = hiltViewModel()
            ApiKeySettingsScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Synapse Plus Settings Screen
        composable(route = SettingsDestination.ROUTE_SYNAPSE_PLUS) {
            SynapsePlusScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Avatar Settings Screen
        composable(route = SettingsDestination.ROUTE_AVATAR) {
            AvatarScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Favourites Settings Screen
        composable(route = SettingsDestination.ROUTE_FAVOURITES) {
            FavouritesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Accessibility Settings Screen
        composable(route = SettingsDestination.ROUTE_ACCESSIBILITY) {
            AccessibilityScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Search Settings (Placeholder)
        composable(route = SettingsDestination.ROUTE_SEARCH) {
            // For now, just show a blank screen or go back
            // Ideally this would be SettingsSearchScreen()
            android.widget.Toast.makeText(LocalContext.current, "Search coming soon", android.widget.Toast.LENGTH_SHORT).show()
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
        }
    }
}

/**
 * Placeholder screen for chat-related features that are not yet implemented.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatPlaceholderScreen(
    title: String,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_revert),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chat feature not implemented",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
