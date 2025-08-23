package com.ozonic.offnbuy.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.viewmodel.ShareUiState
import com.ozonic.offnbuy.viewmodel.ShareViewModel

@Composable
fun ShareScreen(
    sharedText: String?,
    onFinish: () -> Unit
) {
    val viewModel: ShareViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Process the URL when the screen is first composed
    LaunchedEffect(key1 = Unit) {
        viewModel.processSharedUrl(sharedText)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ShareUiState.Loading -> {
                LoadingContent()
            }
            is ShareUiState.Success -> {
                ResultScreen(
                    title = "Link Generated!",
                    message = "Your affiliate link is ready. Use it to shop or share it with friends!",
                    icon = Icons.Default.CheckCircle,
                    iconColor = MaterialTheme.colorScheme.primary,
                    urlToOpen = state.response.data ?: "",
                    urlToShare = state.response.data ?: "",
                    onFinish = onFinish
                )
            }
            is ShareUiState.Error -> {
                // The UI now receives and displays the specific error message from the ViewModel.
                ResultScreen(
                    title = "Something Went Wrong",
                    message = state.message,
                    icon = Icons.Default.Error,
                    iconColor = MaterialTheme.colorScheme.error,
                    urlToOpen = state.originalUrl,
                    urlToShare = state.originalUrl,
                    onFinish = onFinish
                )
            }
        }
    }
}

@Composable
fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Sit back and relax...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "We're generating your affiliate link.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ResultScreen(
    title: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    urlToOpen: String,
    urlToShare: String,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    // Determine if we should show the "Buy Now" and "Share" buttons.
    // We only show them if we have a valid URL to work with.
    val showActionButtons = urlToOpen.startsWith("http")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(0.5f))
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))

//        Icon(
//            painter = painterResource(id = R.drawable.icon),
//            contentDescription = "OffnBuy Logo",
//            tint = MaterialTheme.colorScheme.primary,
//            modifier = Modifier.size(64.dp)
//        )
        Image(
            painter = painterResource(id = R.drawable.icon),
            contentDescription = "OffnBuy Logo",
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Powered by OffnBuy",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- Action Buttons Logic ---
        if (showActionButtons) {
            Button(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, urlToOpen.toUri())
                    context.startActivity(intent)
                    onFinish()
                }
            ) {
                Text("Buy Now")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                onClick = {
                    shareLink(context, urlToShare)
//                    onFinish()
                }
            ) {
                Text("Share Link")
            }
        } else {
            // If there's no valid URL, just show a "Close" button.
            Button(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                onClick = onFinish
            ) {
                Text("Close")
            }
        }
    }
}

// This helper function can stay in this file
fun shareLink(context: Context, url: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Link via")
    context.startActivity(shareIntent)
}