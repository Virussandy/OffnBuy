package com.ozonic.offnbuy.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
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
    WebViewScreen(url = "https://offnbuy.netlify.app/#/terms")
}

@Composable
fun PrivacyPolicyScreen(modifier: Modifier = Modifier) {
    WebViewScreen(url = "https://offnbuy.netlify.app/#/privacy")
}

// In ContentScreen.kt

@Composable
fun HelpAndSupportScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Help & Support",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // --- DISCLAIMER ABOUT DEALS ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "A Note About Deals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        "The deals listed in our app may not always be accurate as they can be for a very short period. We highly recommend you compare the price listed in the deal with the price on the actual website before making a purchase.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // --- FAQ Section ---
        item {
            Text(
                "Frequently Asked Questions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            FaqItem(
                question = "How do notifications work?",
                answer = "Our app sends notifications for new deals. To receive them, please ensure you have granted notification permissions. You can check this in your phone's Settings > Apps > OffnBuy > Notifications."
            )
        }

        item {
            FaqItem(
                question = "How do I generate an affiliate link?",
                answer = "Navigate to the 'Links' tab from the bottom menu. Paste a product URL from a supported store into the text field and tap 'Generate Link'. You can then copy or share your new link."
            )
        }

        item {
            FaqItem(
                question = "Why can't I find a deal when I search?",
                answer = "Our search feature looks through the most recent deals. If a deal is old or has expired, it may no longer appear in search results."
            )
        }

        item {
            FaqItem(
                question = "Does OffnBuy sell these products?",
                answer = "No, OffnBuy does not sell any products. We are a platform that finds and shares deals from various online stores. All purchases are made directly on the respective store's website."
            )
        }

        // --- Contact Us Section ---
        item {
            Text(
                "Contact Us",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        item {
            SupportOption(
                text = "Email Support",
                icon = Icons.Default.Email,
                onClick = { sendSupportEmail(context) }
            )
            HorizontalDivider()
        }
        item {
            SupportOption(
                text = "Report a Bug",
                icon = Icons.Default.BugReport,
                onClick = { reportBugByEmail(context) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
        if (expanded) {
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun SupportOption(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
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
                settings.javaScriptEnabled = true
                loadUrl(url)
            }
        })
        if (isLoading){
            CircularProgressIndicator()
        }
    }
}

// Add this function to your ContentScreen.kt file

private fun reportBugByEmail(context: Context) {
    // Gather device and app version information
    val packageManager = context.packageManager
    val packageName = context.packageName
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    val appVersionName = packageInfo.versionName
    val appVersionCode = packageInfo.versionCode
    val osVersion = Build.VERSION.SDK_INT
    val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"

    // Pre-fill the email body with useful information
    val emailBody = """
        Describe the bug you encountered here...
        
        --------------------
        Device Information (Do not edit)
        App Version: $appVersionName ($appVersionCode)
        Device: $deviceModel
        Android OS: $osVersion
        --------------------
    """.trimIndent()

    // Create the email intent
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri() // Only email apps should handle this
        putExtra(Intent.EXTRA_EMAIL, arrayOf("ozonic.offnbuy@gmail.com")) // Your support email
        putExtra(Intent.EXTRA_SUBJECT, "Bug Report - OffnBuy App v$appVersionName")
        putExtra(Intent.EXTRA_TEXT, emailBody)
    }

    // Use a chooser to let the user pick their email app
    try {
        context.startActivity(Intent.createChooser(intent, "Send Bug Report via..."))
    } catch (e: android.content.ActivityNotFoundException) {
        // Handle the case where no email app is installed
        Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
    }
}


private fun sendSupportEmail(context: Context) {
    // Create the email intent
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri() // Only email apps should handle this
        putExtra(Intent.EXTRA_EMAIL, arrayOf("ozonic.offnbuy@gmail.com")) // Your support email
        putExtra(Intent.EXTRA_SUBJECT, "Support Request - OffnBuy App")
        putExtra(Intent.EXTRA_TEXT, "Please describe your issue or question here:\n\n")
    }

    // Use a chooser to let the user pick their email app
    try {
        context.startActivity(Intent.createChooser(intent, "Send Support Email via..."))
    } catch (e: android.content.ActivityNotFoundException) {
        // Handle the case where no email app is installed
        Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
    }
}