package com.ozonic.offnbuy.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.ozonic.offnbuy.model.DealItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    deals: List<DealItem>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    isInitialLoad: Boolean,
    hasMoreItems: Boolean,
    refreshCount: Int,
    refreshDeals: () -> Unit,
    loadMoreDeals: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {

    val context = LocalContext.current
    val gridState = rememberLazyGridState()


    LaunchedEffect(refreshCount) {
        if (refreshCount > 0) { // Avoid scrolling on initial composition
            gridState.animateScrollToItem(0)
        }
    }

        if (isInitialLoad) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { refreshDeals() },
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
                        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        itemsIndexed(
                            items = deals,
                            key = { _, deal -> "${deal.deal_id}_${deal.posted_on}" },
                            contentType = { _, _ -> "deal_item" }
                        ) { index, deal ->
                            DealCard(
                                deal = deal,
                                onClick = { goToStore(deal, context) },
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

fun goToStore(deal: DealItem, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, deal.url?.toUri())
    context.startActivity(intent)
}