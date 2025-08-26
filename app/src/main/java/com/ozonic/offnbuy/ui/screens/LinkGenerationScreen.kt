package com.ozonic.offnbuy.presentation.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.ozonic.offnbuy.data.local.model.GeneratedLinkEntity
import com.ozonic.offnbuy.data.remote.dto.ApiResponse
import com.ozonic.offnbuy.domain.model.SupportedStore
import com.ozonic.offnbuy.presentation.viewmodel.LinkViewModel
import com.ozonic.offnbuy.presentation.viewmodel.LinkViewModelFactory
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateLinkScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: LinkViewModel = viewModel(factory = LinkViewModelFactory(context))

    val isLoading by viewModel.isLoading.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val supportedStores by viewModel.supportedStores.collectAsState()
    val generatedLinks by viewModel.generatedLinks.collectAsState()

    var productLink by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // This function extracts the first URL from any given text.
    fun extractUrl(text: String?): String? {
        if (text == null) return null
        val pattern = Pattern.compile("(https?://\\S+)")
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(0) else null
    }

    dialogState?.let {
        GenerateLinkDialog(
            apiResponse = it,
            onDismissDialog = { viewModel.onDialogDismiss() },
            context = context
        )
    }

    AppScaffold(
        navController = navController,
        showBottomNav = true,
        topBar = {
            TopAppBar(title = {
                Text(
                    "Generate Link",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { IntroCard() }

            if (supportedStores.isNotEmpty()) {
                item { SupportedStoresRow(stores = supportedStores) }
            }

            item {
                GenerateLinkForm(
                    productLink = productLink,
                    onProductLinkChange = { pastedText ->
                        // When text changes, extract the URL and update the state
                        val extractedUrl = extractUrl(pastedText) ?: pastedText
                        productLink = extractedUrl
                        isError =
                            extractedUrl.isNotBlank() && !android.util.Patterns.WEB_URL.matcher(
                                extractedUrl
                            ).matches()
                    },
                    onGenerateLink = {
                        if (!isError) viewModel.generateLink(productLink)
                    },
                    isLoading = isLoading,
                    isError = isError,
                    context = context
                )
            }

            if (generatedLinks.isNotEmpty()) {
                item {
                    Text(
                        "Your Links",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(generatedLinks, key = { it.url }) { link ->
                    GeneratedLinkItem(
                        link = link,
                        onClick = { viewModel.showDialogForLink(link.url) })
                }
            }
        }
    }
}


@Composable
fun IntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Generate Affiliate Links",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.Handshake,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                "Paste any product URL to create your link. Every click helps cover our server costs and keeps the app free for everyone.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GenerateLinkForm(
    productLink: String,
    onProductLinkChange: (String) -> Unit,
    onGenerateLink: () -> Unit,
    isLoading: Boolean,
    isError: Boolean,
    context: Context
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = productLink,
            onValueChange = onProductLinkChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Paste Product Link Here...") },
            isError = isError,
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (productLink.isBlank()) {
                        IconButton(onClick = {
                            val clipboard = ContextCompat.getSystemService(
                                context,
                                ClipboardManager::class.java
                            )
                            onProductLinkChange(
                                clipboard?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                            )
                        }) {
                            Icon(Icons.Default.ContentPaste, "Paste")
                        }
                    } else {
                        IconButton(onClick = { onProductLinkChange("") }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Uri
            ),
            keyboardActions = KeyboardActions(onDone = { onGenerateLink() }),
//            singleLine = true
        )
        if (isError) {
            Text(
                text = "Please enter a valid URL.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onGenerateLink,
            enabled = !isError && productLink.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = "Generate Link")
            }
        }
    }
}

@Composable
fun SupportedStoresRow(stores: List<SupportedStore>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Supported Stores",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(stores, key = { it.name }) { store ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        AsyncImage(
                            model = store.logoUrl,
                            contentDescription = store.name,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    Text(text = store.name, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun GeneratedLinkItem(link: GeneratedLinkEntity, onClick: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Link, contentDescription = "Link")
            Text(
                text = link.url,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Actions")
        }
    }
}

@Composable
fun GenerateLinkDialog(apiResponse: ApiResponse, onDismissDialog: () -> Unit, context: Context) {
    AlertDialog(
        onDismissRequest = onDismissDialog,
        title = { Text(if (apiResponse.success == 1) "Link Ready" else "Error") },
        text = {
            val message = when {
                apiResponse.data?.contains("could not locate an affiliate URL") == true -> "This store is not supported yet."
                apiResponse.message != null -> apiResponse.message
                apiResponse.data != null -> apiResponse.data
                else -> "An unknown error occurred."
            }
            Text(message)
        },
        confirmButton = {
            if (apiResponse.success == 1 && apiResponse.data?.startsWith("http") == true) {
                // Action buttons for a successful link
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(modifier = Modifier.weight(1f), onClick = {
                            val clipboard = ContextCompat.getSystemService(
                                context,
                                ClipboardManager::class.java
                            )
                            clipboard?.setPrimaryClip(
                                ClipData.newPlainText(
                                    "Generated Link",
                                    apiResponse.data
                                )
                            )
                            Toast.makeText(context, "Link Copied!", Toast.LENGTH_SHORT).show()
                            onDismissDialog()
                        }) {
                            Icon(Icons.Default.ContentCopy, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Copy")
                        }
                        Button(modifier = Modifier.weight(1f), onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, apiResponse.data)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Link"))
                            onDismissDialog()
                        }) {
                            Icon(Icons.Default.Share, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Share")
                        }
                    }
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, apiResponse.data.toUri()))
                        onDismissDialog()
                    }) {
                        Text("Buy Now")
                    }
                }
            } else {
                // Simple dismiss button for errors
                TextButton(onClick = onDismissDialog) { Text("OK") }
            }
        }
    )
}