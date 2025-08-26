// offnbuy/MainActivity.kt

package com.ozonic.offnbuy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.ozonic.offnbuy.presentation.ui.LocalSnackbarHostState
import com.ozonic.offnbuy.presentation.ui.screens.MainScreen
import com.ozonic.offnbuy.presentation.viewmodel.AuthViewModel
import com.ozonic.offnbuy.presentation.viewmodel.AuthViewModelFactory
import com.ozonic.offnbuy.util.InAppUpdateManager
import com.ozonic.offnbuy.util.UpdateState

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
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }

            val updateState by inAppUpdateManager.updateState.collectAsState()
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(this))

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

            dealIdState.value = intent.getStringExtra("deal_id")

            CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                // MainActivity now only calls MainScreen.
                MainScreen(navController = navController, authViewModel = authViewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        AppState.isInForeground = true
    }

    override fun onStop() {
        super.onStop()
        AppState.isInForeground = false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        dealIdState.value = intent.getStringExtra("deal_id")
    }
}

object AppState {
    var isInForeground = false
}