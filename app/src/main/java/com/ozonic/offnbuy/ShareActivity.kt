package com.ozonic.offnbuy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ozonic.offnbuy.ui.screens.ShareScreen
import com.ozonic.offnbuy.ui.theme.OffnBuyTheme

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
                    onFinish = { finish() } // Close the activity when the dialog is dismissed
                )
            }
        }
    }
}