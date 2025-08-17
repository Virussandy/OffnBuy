package com.ozonic.offnbuy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ozonic.offnbuy.ui.screens.MainScreen
import com.ozonic.offnbuy.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val dealIdState = mutableStateOf<String?>(null)
    private val authViewModel: AuthViewModel by viewModels() // Get a reference to the ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // --- THIS IS THE GLOBAL REFRESHER ---
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // This will run every time the app is started or brought to the foreground,
                // ensuring the user's auth state is always fresh.
                authViewModel.refreshUser()
            }
        }

        dealIdState.value = intent.getStringExtra("deal_id")

        setContent {
            MainScreen(
                dealIdState = dealIdState,
                authViewModel = authViewModel // Pass the ViewModel down to the MainScreen
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        dealIdState.value = intent.getStringExtra("deal_id")
    }
}

object AppState {
    var isInForeground = false
}