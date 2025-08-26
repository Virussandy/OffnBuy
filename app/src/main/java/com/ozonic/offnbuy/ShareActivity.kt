package com.ozonic.offnbuy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ozonic.offnbuy.presentation.theme.OffnBuyTheme
import com.ozonic.offnbuy.presentation.ui.screens.ShareScreen

class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedText = if (intent?.action == Intent.ACTION_SEND) {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }

        setContent {
            OffnBuyTheme {
                ShareScreen(
                    sharedText = sharedText,
                    onFinish = { finish() }
                )
            }
        }
    }
}