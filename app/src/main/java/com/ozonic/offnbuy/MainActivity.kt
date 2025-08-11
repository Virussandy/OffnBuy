package com.ozonic.offnbuy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ozonic.offnbuy.ui.screens.MainScreen

class MainActivity : ComponentActivity() {

    private val dealIdState = mutableStateOf<String?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        dealIdState.value = intent.getStringExtra("deal_id")
        setContent {
            MainScreen(
                dealIdState = dealIdState,
            )
        }

    }

    override fun onResume() {
        super.onResume()
        AppState.isInForeground = true
    }

    override fun onPause() {
        super.onPause()
        AppState.isInForeground = false
    }

    override fun onDestroy() {
        super.onDestroy()
//        clearAppCache(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        dealIdState.value = intent.getStringExtra("deal_id")
    }
}


object AppState {
    var isInForeground = false
}



