package com.ozonic.offnbuy.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.util.getTimeAgo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DealCard(
    title: String?,
    imageUrl: String?,
    price: String?,
    originalPrice: String?,
    discount: String?,
    store: String?,
    timeAgo: String?,
    url: String?,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context).data(imageUrl)
            .crossfade(false)   // disable for scrolling lists
            .build()
    }
    val dimenSmall = dimensionResource(R.dimen.small)
    val dimenSmallMedium = dimensionResource(R.dimen.smallMedium)
    val dimenMedium = dimensionResource(R.dimen.medium)

    Box(
        modifier = Modifier.border(
            border = BorderStroke(
                color = MaterialTheme.colorScheme.primary, width = 0.5.dp
            ), shape = MaterialTheme.shapes.medium
        ), contentAlignment = Alignment.TopEnd
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            // Product Image with error handling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.Low,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Title
            TextComposable(
                text = title ?: "",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Price Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (price != "") {
                    TextComposable(
                        text = "₹$price",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                if (originalPrice != "") {
                    TextComposable(
                        text = "₹$originalPrice",
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            // Time and Store Row
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextComposable(
                    text = getTimeAgo(timeAgo),
                )
                Spacer(Modifier.width(dimenSmallMedium))
                TextComposable(
                    text = store ?: "",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Shop Now Button
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                onClick = onClick,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(
                    width = 0.5.dp, color = MaterialTheme.colorScheme.primary
                ),
            ) {
                Text("Shop Now", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(dimenSmall))

            TextComposable(
                modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimenSmall),
                text = "Prices and stock may vary.",
                color = Color.Gray,
            )
        }
        // Top Row: Discount badge and Share icon

        // Discount Badge (Outlined)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimenSmallMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            if (discount?.isNotEmpty() == true) {
                Box(
                    modifier = Modifier
//                        .shadow(
//                            shape = MaterialTheme.shapes.small,
//                            elevation = dimenSmall
//                        )
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small
                        )
                        .border(
                            border = BorderStroke(
                                width = 0.5.dp, color = MaterialTheme.colorScheme.primary
                            ), shape = MaterialTheme.shapes.small
                        )
                ) {
                    TextComposable(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        text = discount ?: "",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            // Share Icon
            Box(
                modifier = Modifier
//                    .shadow(
//                        shape = MaterialTheme.shapes.small,
//                        elevation = dimenSmall
//                    )
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .border(
                        border = BorderStroke(
                            width = 0.5.dp, color = MaterialTheme.colorScheme.primary
                        ), shape = MaterialTheme.shapes.small

                    )
                    .align(Alignment.CenterVertically)

                    .clickable {
                        dealShare(context,title, url)
//                        context.shareImageAndText(
//                            name = title ?: "", imageUrl = imageUrl?:"", shopUrl = url?:"", price = price?:""
//                        )
                    }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .height(28.dp)
                        .width(28.dp)
                        .padding(dimenMedium)
                )
            }
        }
    }
}

fun dealShare(context:Context, title: String?, url: String?){
    val shareText = "${title}\n" + "$url"

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share OffnBuy deal via")
    context.startActivity(shareIntent)
}

//fun Context.shareImageAndText(name: String, imageUrl: String, shopUrl: String, price: String) {
//    CoroutineScope(Dispatchers.IO).launch {
//        try {
//            // Download the image
//            val input = URL(imageUrl).openStream()
//            val file = File(cacheDir, "images")
//            file.mkdirs()
//            val imageFile = File(file, "shared_image.jpg")
//            val output = FileOutputStream(imageFile)
//            input.copyTo(output)
//            input.close()
//            output.close()
//
//            // Get URI using FileProvider
//            val uri = FileProvider.getUriForFile(
//                this@shareImageAndText, "${packageName}.fileProvider", imageFile
//            )
//
//            // Create the intent
//            val shareIntent = Intent(Intent.ACTION_SEND).apply {
//                type = "image/*"
//                putExtra(Intent.EXTRA_STREAM, uri)
//                putExtra(Intent.EXTRA_TEXT, "$name\n\n$price\n\n$shopUrl")
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            }
//
////            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
////            val clip = ClipData.newPlainText("deal", "$name\n₹$price\n$shopUrl")
////            clipboard.setPrimaryClip(clip)
//
//
//            withContext(Dispatchers.Main) {
//                startActivity(Intent.createChooser(shareIntent, "Share via"))
//            }
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            withContext(Dispatchers.Main) {
//                Toast.makeText(this@shareImageAndText, "Failed to share image", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
//    }
//}

@Composable
fun TextComposable(
    modifier: Modifier = Modifier,
    text: String,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    style: TextStyle = MaterialTheme.typography.labelSmall,
    textAlign: TextAlign = TextAlign.Center,
    textDecoration: TextDecoration = TextDecoration.None,
    minLines: Int = 1,
    maxLines: Int = 2
) {

    Text(
        modifier = modifier,
        text = text,
        fontWeight = fontWeight,
        color = color,
        style = style,
        textAlign = textAlign,
        textDecoration = textDecoration,
        minLines = minLines,
        maxLines = maxLines
    )
}