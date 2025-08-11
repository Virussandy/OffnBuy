package com.ozonic.offnbuy.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ozonic.offnbuy.model.ApiResponse
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri

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
    isLoading: Boolean
) {
    val context = LocalContext.current

    if (generatedLink != null) {
        GenerateLinkDialog(
            apiResponse = generatedLink,
            onDismissDialog = onDismissDialog,
            context = context
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Generate Your Link & Help Keep the App Running",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Icon(
            modifier = Modifier.fillMaxWidth(),
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            "Paste any product URL below to create your affiliate link. Every click and purchase helps cover our server costs and keep the app free for everyone.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal
        )
        GenerateLinkBar(
            productLink = productLink,
            onProductLinkChange = { it -> onProductLinkChange(it) },
            clipboardPaste = clipboardPaste,
            onGenerateLink = onGenerateLink
        )
        Button(onClick = {
            onGenerateLink()
        }) {
            Text(text = "Generate Link")
        }
        HorizontalDivider()
        if (recentLinks.isEmpty()) {
            Text("No links generated yet", color = Color.Gray)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(recentLinks) { link ->
                    Text(text = link, modifier = Modifier.clickable {
                        listLinkClick(link)
                    })
                }
            }
        }
    }
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 1.0.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun GenerateLinkDialog(apiResponse: ApiResponse, onDismissDialog: () -> Unit, context: Context) {
    AlertDialog(
        onDismissRequest = onDismissDialog,
        title = { Text(if (apiResponse.success == 1) "Link Generated!" else "Error") },
        text = {Text(apiResponse.data ?: apiResponse.message ?: "An Unknown Error Occurred")},
        confirmButton = {
            Column {
                if(apiResponse.success == 1 && apiResponse.data != null){
                    Row(horizontalArrangement = Arrangement.SpaceEvenly){
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                            val clipBoard = getSystemService(context, ClipboardManager::class.java)
                            val clip = ClipData.newPlainText("Generated Link", apiResponse.data)
                            clipBoard?.setPrimaryClip(clip)
                            onDismissDialog()
                        }){
                            Icon(modifier = Modifier.padding(horizontal = 8.dp), imageVector = Icons.Filled.ContentCopy, contentDescription = null)
                            Text("Copy")
                        }

                        Spacer(Modifier.width(8.dp))

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, apiResponse.data)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                            onDismissDialog()
                        }) {
                            Icon(modifier = Modifier.padding(horizontal = 8.dp), imageVector = Icons.Filled.Share, contentDescription = null)
                            Text("Share")
                        }
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, apiResponse.data.toUri())
                        context.startActivity(intent)
                        onDismissDialog()
                    }) {
                        Text("Buy Now")
                    }
                }else{
                    Button(onClick = onDismissDialog) {
                        Text("OK")
                    }
                }
            }
        }
    )
}

@Composable
fun GenerateLinkBar(
    productLink: String,
    onProductLinkChange: (String) -> Unit,
    clipboardPaste: () -> Unit,
    onGenerateLink: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current


    TextField(
        value = productLink,
        onValueChange = { onProductLinkChange(it) },
        placeholder = { Text("Paste Product Link Here...") },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { clipboardPaste() }) {
                Icon(Icons.Default.ContentPaste, contentDescription = null)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(25.dp)
            )
            .border(
                border = BorderStroke(
                    width = 0.5.dp, color = MaterialTheme.colorScheme.primary
                ), shape = RoundedCornerShape(25.dp)
            ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Uri
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                onGenerateLink()
            },
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,  // <-- remove underline
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}