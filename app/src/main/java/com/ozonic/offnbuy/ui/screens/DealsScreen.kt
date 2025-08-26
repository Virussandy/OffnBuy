package com.ozonic.offnbuy.presentation.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ozonic.offnbuy.di.ViewModelModule
import com.ozonic.offnbuy.presentation.viewmodel.DealsViewModel
import com.ozonic.offnbuy.presentation.viewmodel.DealsViewModelFactory
import com.ozonic.offnbuy.ui.screens.DealCard
import com.ozonic.offnbuy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(navController: NavController, viewModel: DealsViewModel = viewModel()) {
    val context = LocalContext.current

    val deals by viewModel.deals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isInitialLoad by viewModel.isInitialLoad.collectAsState()
    val hasMoreItems by viewModel.hasMoreItems.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val newAvailable by viewModel.newDealAvailable.collectAsState()
    val refreshCount by viewModel.refreshCount.collectAsState()

    val gridState = rememberLazyGridState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val isFabVisible = remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0) isFabVisible.value = false
                else if (available.y > 0) isFabVisible.value = true
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(refreshCount) {
        if (refreshCount > 0) gridState.animateScrollToItem(0)
    }

    AppScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        navController = navController,
        showBottomNav = true,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "OffnBuy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }, scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background, // Set this to the same color
                ),
                navigationIcon = {
                    Image(
                        painter = painterResource(R.drawable.icon), contentDescription = null,
                        modifier = Modifier.padding(
                            start = 8.dp, end = 4.dp, top = 16.dp, bottom = 16.dp
                        ),
                    )
                })
        },
        floatingActionButton = {
            if (newAvailable) {
                ExtendedFloatingActionButton(
                    expanded = isFabVisible.value,
                    onClick = { viewModel.refreshDeals() },
                    icon = { Icon(Icons.Default.Refresh, "refresh") },
                    text = { Text("Refresh Deals") })
            }
        }) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshDeals() },
            modifier = Modifier.padding(paddingValues)
        ) {
            // Screen content remains the same...
            if (isInitialLoad && deals.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else if (deals.isEmpty() && !isLoading && !isInitialLoad && !isRefreshing) {
                Box(
                    Modifier.fillMaxSize(), Alignment.Center
                ) { Text("No Deals Found, Pull Down to Refresh") }
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    itemsIndexed(
                        deals, key = { _, d -> "${d.deal_id}_${d.posted_on}" }) { index, deal ->
                        DealCard(deal = deal, onClick = { goToStore(deal.url, context) })
                        if (index >= deals.size - 12 && hasMoreItems && !isLoading) {
                            LaunchedEffect(index) { viewModel.loadMoreDeals() }
                        }
                    }
                    if (isLoading) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp), Alignment.Center
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

fun goToStore(dealUrl: String?, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, dealUrl?.toUri())
    context.startActivity(intent)
}