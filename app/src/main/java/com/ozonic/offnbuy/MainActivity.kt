// offnbuy/MainActivity.kt

package com.ozonic.offnbuy

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.ozonic.offnbuy.di.AppModule
import com.ozonic.offnbuy.di.DataModule
import com.ozonic.offnbuy.presentation.ui.LocalSnackbarHostState
import com.ozonic.offnbuy.presentation.ui.screens.MainScreen
import com.ozonic.offnbuy.presentation.ui.screens.SettingsState
import com.ozonic.offnbuy.presentation.viewmodel.AuthViewModel
import com.ozonic.offnbuy.presentation.viewmodel.AuthViewModelFactory
import com.ozonic.offnbuy.presentation.viewmodel.SettingsViewModel
import com.ozonic.offnbuy.presentation.viewmodel.SettingsViewModelFactory
import com.ozonic.offnbuy.util.InAppUpdateManager
import com.ozonic.offnbuy.util.NotificationSyncManager
import com.ozonic.offnbuy.util.UpdateState
import com.ozonic.offnbuy.util.UserDataManager

class MainActivity : ComponentActivity() {

    private val dealIdState = mutableStateOf<String?>(null)
    private lateinit var inAppUpdateManager: InAppUpdateManager
    private lateinit var userDataManager: UserDataManager
    private lateinit var notificationSyncManager: NotificationSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        inAppUpdateManager = InAppUpdateManager(this)
        lifecycle.addObserver(inAppUpdateManager)

        val sharedPrefManager = AppModule.provideSharedPrefManager(this)

        setContent {
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            val context = LocalContext.current

            val updateState by inAppUpdateManager.updateState.collectAsState()
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(this))

            LaunchedEffect(authViewModel) {
                userDataManager = UserDataManager(
                    scope = lifecycleScope,
                    authViewModel = authViewModel,
                    userDataRepository = DataModule.provideUserDataRepository(context)
                )
                userDataManager.start()

                notificationSyncManager = NotificationSyncManager(
                    notificationRepository = DataModule.provideNotificationRepository(context)
                )
                notificationSyncManager.start()
            }

            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(
                    application = LocalContext.current.applicationContext as Application,
                    authViewModel = authViewModel,
                    sharedPrefManager = AppModule.provideSharedPrefManager(LocalContext.current)
                )
            )

            DisposableEffect(lifecycle) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        settingsViewModel.checkNotificationStatus()
                    }
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted: Boolean ->
                    sharedPrefManager.setHasAskedForNotifications(true)
                }
            )

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !sharedPrefManager.hasAskedForNotifications()) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }

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
                MainScreen(navController = navController, authViewModel = authViewModel, settingsViewModel = settingsViewModel)
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