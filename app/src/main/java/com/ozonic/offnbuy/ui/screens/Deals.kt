package com.ozonic.offnbuy.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.navigation.BottomNavBar
import com.ozonic.offnbuy.viewmodel.DealsViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    deals: List<DealItem>,
    newAvailable: Boolean,
    isLoading: Boolean,
    isRefreshing: Boolean,
    isInitialLoad: Boolean,
    hasMoreItems: Boolean,
    refreshCount: Int,
    onSearchButtonClick: () -> Unit,
    loadMoreDeals: () -> Unit,
    refreshDeals: () -> Unit,
) {

    val context = LocalContext.current
    val gridState = rememberLazyGridState()

    LaunchedEffect(refreshCount) {
        snapshotFlow { deals.firstOrNull()?.deal_id }.collectLatest {
            gridState.scrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
//                    Image(modifier = Modifier.padding(12.dp), painter = painterResource(R.drawable.icon), contentDescription = "OffnBuy Icon")
                    Icon(
                        modifier = Modifier.padding(12.dp),
                        painter = painterResource(R.drawable.icon),
                        contentDescription = "Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Off",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "n",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.inversePrimary
                        )
                        Text(
                            "Buy",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }, actions = {
                    IconButton(onClick = { onSearchButtonClick() }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }, windowInsets = WindowInsets(
                    top = dimensionResource(id = R.dimen.none),
                    bottom = dimensionResource(id = R.dimen.none)
                ),
                expandedHeight = 50.dp
            )
        },

        floatingActionButton = {
            if (newAvailable) {
                FloatingActionButton(
                    modifier = Modifier.padding(
                        bottom = dimensionResource(R.dimen.smallMedium),
                        end = dimensionResource(R.dimen.smallMedium)
                    ), onClick = {
                        refreshDeals()
                    }) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "refresh")
                        Text(
                            "Refresh for New Deals",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        if (isInitialLoad) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { refreshDeals() },
                modifier = Modifier.padding(PaddingValues(top = paddingValues.calculateTopPadding()))
            ) {
                if (deals.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No Deals Found, Pull Down to Refresh")
                    }
                } else {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(
                            items = deals,
                            key = { _, deal -> "${deal.deal_id}_${deal.posted_on}" },
                            contentType = { _, _ -> "deal_item" }
                        ) { index, deal ->
                            DealCard(
                                title = deal.title ?: "",
                                imageUrl = deal.image ?: "",
                                price = deal.price ?: "",
                                originalPrice = deal.originalPrice ?: "",
                                discount = deal.discount ?: "",
                                store = deal.store ?: "",
                                timeAgo = deal.posted_on ?: "",
                                onClick = { goToStore(deal, context) },
                                url = deal.url ?: "",
                            )

                            if (index >= deals.size - 12 && hasMoreItems && !isLoading) {
                                LaunchedEffect(index) {
                                    loadMoreDeals()
                                }
                            }
                        }

                        if (isLoading) {
                            item(span = { GridItemSpan(2) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun goToStore(deal: DealItem, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, deal.url?.toUri())
    context.startActivity(intent)
}




