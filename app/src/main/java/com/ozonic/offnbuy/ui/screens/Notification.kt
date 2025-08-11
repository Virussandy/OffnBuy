package com.ozonic.offnbuy.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.model.NotifiedDeal
import com.ozonic.offnbuy.util.getTimeAgo
import com.ozonic.offnbuy.viewmodel.NotificationViewModel
import kotlinx.coroutines.delay

fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> throw IllegalStateException("Context is not an Activity")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = viewModel(),
    navController: NavController
) {

    val notifications by viewModel.notifiedDeals.collectAsState()
    val context = LocalContext.current
    val notificationCount by viewModel.unseenCount.collectAsState()
    var expanded by remember { mutableStateOf(false) }

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

    Scaffold(
        modifier = Modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                },
                actions = {

                        IconButton(onClick = {
                            expanded = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Option",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mark All As Read") },
                            onClick = {
                                expanded = false
                                if(notificationCount>0){
                                    viewModel.markAllAsSeen()
                                }
                            }
                        )
                    }
                },
                windowInsets = WindowInsets(
                    top = dimensionResource(id = R.dimen.none),
                    bottom = dimensionResource(id = R.dimen.none),
                ),
            )
        }
    ) { paddingValues ->
        if(notificationCount<1){
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)){
                Text(text = "No Notifications", modifier = Modifier.align(Alignment.Center))
            }
        }else{
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(
                    items = notifications,
                    key = { index, notification -> "${notification.deal}_${notification.timestamp}" }) { index, notification ->
                    NotificationItems(
                        index = notification,
                        viewModel = viewModel,
                        navController = navController,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun NotificationItems(
    index: NotifiedDeal,
    viewModel: NotificationViewModel,
    navController: NavController,
) {

    val image =
        ImageRequest.Builder(LocalContext.current).data(index.deal.image).crossfade(false).build()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clickable {
                viewModel.markAsSeen(index.deal.deal_id)
                navController.navigate("dealDetail/${index.deal.deal_id}")
            },
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            width = 0.5.dp, color = MaterialTheme.colorScheme.primary
        ),
//        colors = if(!index.isSeen) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary) else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(60.dp)
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = image,
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.Low,
                    contentDescription = index.deal.title,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = index.deal.title?:"",
                    style = MaterialTheme.typography.labelMedium,
                    minLines = 1,
                    maxLines = 2,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = index.deal.discount ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "₹${index.deal.originalPrice}" ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Thin,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "₹${index.deal.price}" ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = getTimeAgo(index.deal.posted_on),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Normal
                )
            }
            if (!index.isSeen) {
                Badge(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            }
        }
    }
}