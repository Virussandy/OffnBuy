package com.ozonic.offnbuy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ozonic.offnbuy.ui.screens.MainScreen
import com.ozonic.offnbuy.util.InAppUpdateManager
import com.ozonic.offnbuy.util.UpdateState
import com.ozonic.offnbuy.viewmodel.AuthViewModel
import com.ozonic.offnbuy.viewmodel.MainViewModel
import com.ozonic.offnbuy.viewmodel.SettingsViewModel
import com.ozonic.offnbuy.viewmodel.SettingsViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val dealIdState = mutableStateOf<String?>(null)
    private lateinit var inAppUpdateManager: InAppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        inAppUpdateManager = InAppUpdateManager(this)
        lifecycle.addObserver(inAppUpdateManager)

        setContent {
            // The AuthViewModel is now created here, within the composable scope
            val authViewModel: AuthViewModel = viewModel()
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(application, authViewModel)
            )
            val mainViewModel: MainViewModel = viewModel()

            val updateState by inAppUpdateManager.updateState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(updateState) {
                if (updateState is UpdateState.UpdateReadyToInstall) {
                    val result = snackbarHostState.showSnackbar(
                        message = "A new update is ready to install.",
                        actionLabel = "Restart",
                        duration = SnackbarDuration.Indefinite
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        (updateState as UpdateState.UpdateReadyToInstall).manager.completeUpdate()
                    }
                }
            }

            // --- THIS IS THE GLOBAL REFRESHER ---
            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        authViewModel.refreshUser()
                        settingsViewModel.checkNotificationStatus(application)
                    }
                }
            }

            dealIdState.value = intent.getStringExtra("deal_id")

            MainScreen(
                dealIdState = dealIdState,
                authViewModel = authViewModel,
                settingsViewModel = settingsViewModel,
                mainViewModel = mainViewModel,
                snackbarHostState = snackbarHostState
            )
        }
    }
    // These standard Android lifecycle methods will correctly update the AppState.
    override fun onStart() {
        super.onStart()
        AppState.isInForeground = true
    }

    override fun onStop() {
        super.onStop()
        AppState.isInForeground = false
    }
    // --- END OF FIX ---

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        dealIdState.value = intent.getStringExtra("deal_id")
    }
}

/**
 * A simple global object to track the app's foreground/background state.
 * This is used by MyFirebaseMessagingService to decide if a system notification should be shown.
 */
object AppState {
    var isInForeground = false
}