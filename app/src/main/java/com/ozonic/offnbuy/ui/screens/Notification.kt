package com.ozonic.offnbuy.ui.screens

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.ozonic.offnbuy.model.NotifiedDeal
import com.ozonic.offnbuy.util.getTimeAgo
import com.ozonic.offnbuy.viewmodel.NotificationViewModel
import com.ozonic.offnbuy.viewmodel.SettingsViewModel
import io.ktor.http.ContentType
import kotlinx.coroutines.delay

fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> throw IllegalStateException("Context is not an Activity")
}

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = viewModel(),
    settingsViewModel: SettingsViewModel,
    navController: NavController,
    onLoadMore: () -> Unit,
    hasMore: Boolean,
    isLoading: Boolean
) {
    val notifications by viewModel.notifiedDeals.collectAsState()
    val isFirstTime by viewModel.isFirstTime.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    if(isFirstTime){
        LaunchedEffect(Unit) {
            delay(1000L)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    context.findActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    if(isFirstTime){
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                // When the app is resumed (e.g., after returning from the permission dialog)
                if (event == Lifecycle.Event.ON_RESUME) {
                    // We explicitly tell the SettingsViewModel to re-check the status.
                    settingsViewModel.checkNotificationStatus(context.applicationContext as Application)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            // Clean up the observer when the composable is disposed.
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }


    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No Notifications", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(notifications, key = { _, it -> it.deal.deal_id }) { index, notification ->
                NotificationItem(
                    notification = notification,
                    onClick = {
                        viewModel.markAsSeen(notification.deal.deal_id)
                        navController.navigate("dealDetail/${notification.deal.deal_id}")
                    }
                )
                if (index >= notifications.size - 3 && hasMore && !isLoading) {
                    LaunchedEffect(key1 = index) {
                        onLoadMore()
                    }
                }
            }
            if (isLoading && notifications.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: NotifiedDeal,
    onClick: () -> Unit,
) {
    val cardColors = if (!notification.isSeen) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = cardColors
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image
            AsyncImage(
                model = notification.deal.image,
                contentDescription = notification.deal.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(76.dp)
                    .clip(MaterialTheme.shapes.medium)
            )

            // Content Column
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = notification.deal.title ?: "New Deal Available",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Pricing Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if(notification.deal.price!=null) "₹${notification.deal.price}" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if(notification.deal.originalPrice!=null) "₹${notification.deal.originalPrice}" else "",
                        style = MaterialTheme.typography.labelSmall.copy(
                            textDecoration = TextDecoration.LineThrough
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Store and Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = notification.deal.store ?: "Store",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = getTimeAgo(notification.deal.posted_on),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                }
            }
        }
    }
}