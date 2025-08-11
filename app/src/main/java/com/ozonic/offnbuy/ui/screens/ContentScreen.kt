package com.ozonic.offnbuy.ui.screens

import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.ozonic.offnbuy.model.ContentType

@Composable
fun ContentScreen(contentType: ContentType, modifier: Modifier = Modifier) {
    when (contentType) {
        ContentType.TermsAndConditions -> TermsAndConditionsScreen()
        ContentType.PrivacyPolicy -> PrivacyPolicyScreen()
        ContentType.HelpAndSupport -> HelpAndSupportScreen()
    }
}

@Composable
fun TermsAndConditionsScreen(modifier: Modifier = Modifier) {
    WebViewScreen(url = "https://example.com/terms-and-conditions")
}

@Composable
fun PrivacyPolicyScreen(modifier: Modifier = Modifier) {
    WebViewScreen(url = "https://example.com/privacy-policy")
}

@Composable
fun HelpAndSupportScreen(modifier: Modifier = Modifier) {
    Text("Help and Support Screen")
}

@Composable
fun WebViewScreen(url: String) {
    var isLoading by remember { mutableStateOf(true) }
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        isLoading = true
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isLoading = false
                    }
                }
                loadUrl(url)
            }
        })
        if (isLoading){
            CircularProgressIndicator()
        }
    }
}