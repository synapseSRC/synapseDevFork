package com.synapse.social.studioasinc.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synapse.social.studioasinc.R

/**
 * About and Support Settings screen.
 *
 * Displays app information, version details, and provides access to support
 * resources including Terms of Service, Privacy Policy, Help Center, feedback
 * submission, update checking, and open source licenses.
 *
 * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSupportScreen(
    onBackClick: () -> Unit,
    onNavigateToLicenses: () -> Unit = {},
    viewModel: AboutSupportViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val appVersion by viewModel.appVersion.collectAsState()
    val buildNumber by viewModel.buildNumber.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()

    // Show error/message snackbar if present
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text("About & Support") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = SettingsSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(SettingsSpacing.sectionSpacing),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // App Logo and Version Header Card
            item {
                AppInfoHeaderCard(
                    appVersion = appVersion,
                    buildNumber = buildNumber
                )
            }

            // Legal Section
            item {
                SettingsSection(title = "Legal") {
                    SettingsNavigationItem(
                        title = "Terms of Service",
                        subtitle = "View our terms and conditions",
                        icon = R.drawable.ic_docs_48px,
                        onClick = {
                            val url = viewModel.getTermsOfServiceUrl()
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        title = "Privacy Policy",
                        subtitle = "Learn how we protect your data",
                        icon = R.drawable.ic_shield_lock,
                        onClick = {
                            val url = viewModel.getPrivacyPolicyUrl()
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Support Section
            item {
                SettingsSection(title = "Support") {
                    SettingsNavigationItem(
                        title = "Help Center",
                        subtitle = "FAQs and support resources",
                        icon = R.drawable.ic_info_48px,
                        onClick = {
                            val url = viewModel.getHelpCenterUrl()
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        title = "Report a Problem",
                        subtitle = "Send us feedback or report issues",
                        icon = R.drawable.ic_bug_report_48px,
                        onClick = {
                            val url = "https://github.com/synapseSRC/synapseApp/issues/new?template=bug_report.md"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // App Updates Section
            item {
                SettingsSection(title = "Updates") {
                    SettingsNavigationItem(
                        title = "Check for Updates",
                        subtitle = "See if a new version is available",
                        icon = R.drawable.ic_download,
                        onClick = {
                            val url = "https://synapsesocial.vercel.app"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Licenses Section
            item {
                SettingsSection(title = "Licenses") {
                    SettingsNavigationItem(
                        title = "Open Source Licenses",
                        subtitle = "View third-party library attributions",
                        icon = R.drawable.ic_bug_report_48px,
                        onClick = {
                            viewModel.navigateToLicenses()
                            onNavigateToLicenses()
                        }
                    )
                }
            }

            // Copyright Footer
            item {
                Text(
                    text = "© 2024 Synapse Social. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        }
    }

}

/**
 * App information header card displaying logo and version info.
 *
 * Displays the app logo, version name, and build number in a centered
 * card with elevated background.
 *
 * @param appVersion The app version string
 * @param buildNumber The build number string
 *
 * Requirements: 9.1
 */
@Composable
private fun AppInfoHeaderCard(
    appVersion: String,
    buildNumber: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SettingsShapes.cardShape,
        color = SettingsColors.cardBackgroundElevated,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SettingsSpacing.profileHeaderPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Logo
            Image(
                painter = painterResource(R.drawable.synapse_logo_small),
                contentDescription = "Synapse Logo",
                modifier = Modifier.size(80.dp)
            )

            // App Name
            Text(
                text = "Synapse",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Version Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Version $appVersion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Build $buildNumber",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

