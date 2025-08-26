package com.ozonic.offnbuy.presentation.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.ozonic.offnbuy.di.DataModule
import com.ozonic.offnbuy.domain.model.Deal
import com.ozonic.offnbuy.presentation.viewmodel.DealDetailUiState
import com.ozonic.offnbuy.presentation.viewmodel.DealDetailViewModel
import com.ozonic.offnbuy.presentation.viewmodel.DealDetailViewModelFactory
import com.ozonic.offnbuy.util.getTimeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealDetailScreen(dealId: String, navController: NavController) {
    val context = LocalContext.current
    val viewModel: DealDetailViewModel = viewModel(
        factory = DealDetailViewModelFactory(dealId, DataModule.provideDealsRepository(context))
    )
    val uiState by viewModel.uiState.collectAsState()

    AppScaffold(
        navController = navController,
        showBottomNav = false,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Deal Details", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            if (uiState is DealDetailUiState.Success) {
                val deal = (uiState as DealDetailUiState.Success).deal
                ExtendedFloatingActionButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, deal.url?.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    text = { Text(text = "Shop Now", fontWeight = FontWeight.Bold) }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DealDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is DealDetailUiState.Success -> {
                    DealDetailContent(deal = state.deal)
                }

                is DealDetailUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun DealDetailContent(deal: Deal) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Add padding for the FAB and the system navigation bar
        contentPadding = PaddingValues(
            bottom = 96.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        item {
            AsyncImage(
                model = deal.image,
                contentDescription = deal.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
        item {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = deal.title ?: "No Title",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    if (!deal.price.isNullOrBlank()) {
                        Text(
                            text = "₹${deal.price}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    if (!deal.originalPrice.isNullOrBlank()) {
                        Text(
                            text = "₹${deal.originalPrice}",
                            style = MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.LineThrough),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (!deal.discount.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text(
                                text = deal.discount,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                InfoChip(
                    icon = Icons.Default.Storefront,
                    title = "Store",
                    subtitle = deal.store ?: "Unknown"
                )
                InfoChip(
                    icon = Icons.Default.Schedule,
                    title = "Posted",
                    subtitle = getTimeAgo(deal.posted_on)
                )
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}