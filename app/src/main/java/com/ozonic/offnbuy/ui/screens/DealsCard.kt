package com.ozonic.offnbuy.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.util.getTimeAgo

@Composable
fun DealCard(
    deal: DealItem,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val imageRequest = ImageRequest.Builder(context)
        .data(deal.image)
        .crossfade(true)
        .build()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column {
            // --- Image Section with Discount Badge ---
            Box {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = deal.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
                // Discount Badge
                if (!deal.discount.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.TopStart),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = deal.discount,
                            style = MaterialTheme.typography.labelSmall,
//                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // --- Content Section ---
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = deal.title ?: "No Title",
                    style = MaterialTheme.typography.labelMedium,
//                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if(deal.price.isNullOrBlank()){
                            ""
                        } else {
                            "₹${deal.price}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if(deal.originalPrice.isNullOrBlank()){
                            ""
                        }else{
                            "₹${deal.originalPrice}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            textDecoration = TextDecoration.LineThrough
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Store and Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = deal.store ?: "Store",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.height(12.dp))
                    Text(
                        text = getTimeAgo(deal.posted_on),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

            // --- Action Section ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ElevatedButton (
                    onClick = onClick,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Shop Now")
                }
                IconButton(onClick = { dealShare(context, deal.title, deal.url) }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

fun dealShare(context: Context, title: String?, url: String?) {
    val shareText = "${title}\n${url}"

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share OffnBuy deal via")
    context.startActivity(shareIntent)
}