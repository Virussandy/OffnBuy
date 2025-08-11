package com.ozonic.offnbuy.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.google.firebase.firestore.FirebaseFirestore
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.util.getTimeAgo

@Composable
fun DealDetailScreen(dealId: String, context: Context) {
    var deal by remember { mutableStateOf<DealItem?>(null) }

    LaunchedEffect(dealId) {
        FirebaseFirestore.getInstance().collection("deals").document(dealId).get()
            .addOnSuccessListener {
                deal = it.toObject(DealItem::class.java)
            }
    }

    deal?.let {
        // Show deal UI here
        DealView(modifier = Modifier, it = it, context = context)
    } ?: run {
        Text("Loading...", modifier = Modifier.padding(16.dp))
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DealView(modifier: Modifier = Modifier, it: DealItem, context: Context) {
    Box(modifier = Modifier.padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(it.image)
                            .crossfade(true)
                            .build(),
                        contentScale = ContentScale.Fit,
                        filterQuality = FilterQuality.Low,
                        contentDescription = it.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Text(
                    it.title ?: "",
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        it.originalPrice?:"",
                        textDecoration = TextDecoration.LineThrough,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        it.price?:"",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(getTimeAgo(it.posted_on?:""), style = MaterialTheme.typography.labelSmall)
                    Text(
                        it.store?:"Others",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, it.redirectUrl?.toUri())
                    context.startActivity(intent)
                }) {
                    Text("Buy Now")
                }
            }
        }
        Surface(modifier = Modifier.padding(8.dp), shape = MaterialTheme.shapes.medium) {
            Box(modifier = Modifier.padding(8.dp)) {
                Text(
                    it.discount?:"No Discount",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

}
