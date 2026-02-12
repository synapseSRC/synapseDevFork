package com.synapse.social.studioasinc.feature.shared.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
// SoundManager is currently missing/deleted, commenting out usage to fix build
// import com.synapse.social.studioasinc.core.util.SoundManager
import com.synapse.social.studioasinc.feature.shared.navigation.AppNavigation
import com.synapse.social.studioasinc.feature.shared.navigation.AppDestination
import com.synapse.social.studioasinc.feature.shared.theme.SynapseTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // @Inject
    // lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            SynapseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination by viewModel.startDestination.collectAsState()

                    if (startDestination != null) {
                        // Passing null or dummy for SoundManager if strictly required by constructor,
                        // but AppNavigation expects it.
                        // I need to update AppNavigation to handle nullable or remove SoundManager dependency.
                        // Assuming AppNavigation signature needs update too if I remove it here.
                        // For now, let's look at AppNavigation signature in previous steps...
                        // it was .
                        // I will update AppNavigation to not require SoundManager temporarily.

                        // BUT, to keep it simple, I'll update AppNavigation to accept nullable SoundManager?
                        // Or just remove it. Removing is safer if class is gone.
                        // Let's modify AppNavigation first.
                        AppNavigation(
                            navController = navController
                            // soundManager = soundManager
                        )
                    }
                }
            }
        }
    }
}
