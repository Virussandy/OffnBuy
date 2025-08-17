package com.ozonic.offnbuy.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri
import com.ozonic.offnbuy.model.ApiResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateLink(
    recentLinks: List<String>,
    productLink: String,
    onGenerateLink: () -> Unit,
    clipboardPaste: () -> Unit,
    onProductLinkChange: (String) -> Unit,
    listLinkClick: (String) -> Unit,
    generatedLink: ApiResponse?,
    onDismissDialog: () -> Unit,
    isLoading: Boolean,
    isError: Boolean,
) {
    val context = LocalContext.current

    if (generatedLink != null) {
        GenerateLinkDialog(
            apiResponse = generatedLink,
            onDismissDialog = onDismissDialog,
            context = context
        )
    }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
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

            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = productLink,
                        onValueChange = onProductLinkChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Paste Product Link Here...") },
//                        singleLine = true,
                        isError = isError,
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isError) {
                                    Icon(Icons.Filled.Error, "error", tint = MaterialTheme.colorScheme.error)
                                }
                                IconButton(onClick = clipboardPaste) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste from clipboard")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Uri
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onGenerateLink() }
                        )
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
                        enabled = !isError && productLink.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Generate Link")
                    }
                }
            }

            if (recentLinks.isEmpty()) {
                item {
                    Text(
                        text = "Recent Links",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )

                    HorizontalDivider()

                    Text(
                        text = "No links generated yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )
                }
            }

            if (recentLinks.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Links",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                items(recentLinks) { link ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { listLinkClick(link) }
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = link,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingContent = {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Open Link"
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun GenerateLinkDialog(apiResponse: ApiResponse, onDismissDialog: () -> Unit, context: Context) {
    AlertDialog(
        onDismissRequest = onDismissDialog,
        title = { Text(if (apiResponse.success == 1) "Link Generated!" else "Error") },
        text = { Text(apiResponse.data ?: apiResponse.message ?: "An Unknown Error Occurred") },
        confirmButton = {
            if (apiResponse.success == 1 && apiResponse.data != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val clipBoard = getSystemService(context, ClipboardManager::class.java)
                                val clip = ClipData.newPlainText("Generated Link", apiResponse.data)
                                clipBoard?.setPrimaryClip(clip)
                                onDismissDialog()
                            }
                        ) {
                            Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Copy")
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, apiResponse.data)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                                onDismissDialog()
                            }
                        ) {
                            Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Share")
                        }
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, apiResponse.data.toUri())
                            context.startActivity(intent)
                            onDismissDialog()
                        }
                    ) {
                        Text("Buy Now")
                    }
                }
            } else {
                TextButton(onClick = onDismissDialog) {
                    Text("OK")
                }
            }
        }
    )
}