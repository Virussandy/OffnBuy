package com.ozonic.offnbuy.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.model.SearchResultStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchDeals: List<DealItem>,
    isLoading: Boolean,
    searchQuery: String,
    hasMoreItems: Boolean,
    searchStatus: SearchResultStatus,
    onSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    loadMoreDeals: () -> Unit,
    onBackOperation: () -> Unit
) {

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(Unit) {
        if (searchDeals.isNotEmpty()) {
            listState.scrollToItem(0)
        }
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Search",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackOperation) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp),
                    scrollBehavior = scrollBehaviour,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )

                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { onSearchQueryChange(it) },
                    onSearch = { onSearch() },
                    listState = listState,
                    focusRequester = focusRequester,
                    scope = scope,
                )
            }
        },
        contentWindowInsets = WindowInsets(left = dimensionResource(R.dimen.none), right = dimensionResource(R.dimen.none), top = dimensionResource(R.dimen.none), bottom = dimensionResource(R.dimen.none))
    ) { paddingValues ->
            LazyVerticalGrid(
                state = listState,
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
            ) {
                itemsIndexed(
                    items = searchDeals,
                    key = { _, deal -> "${deal.deal_id}_${deal.posted_on}" }
                ) { index, deal ->
                    DealCard(
                        deal = deal,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, deal.url?.toUri())
                            context.startActivity(intent)
                        },
                    )

                    if (index >= searchDeals.size - 12 && hasMoreItems && !isLoading) {
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

                if (!isLoading) {
                    when (searchStatus) {
                        SearchResultStatus.NoResults -> {
                            item(span = { GridItemSpan(2) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No Deals Found")
                                }
                            }
                        }

                        SearchResultStatus.EndReached -> {
                            item(span = { GridItemSpan(2) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No more deals")
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    listState: LazyGridState,
    focusRequester: FocusRequester,
    scope: CoroutineScope,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(25.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { onSearchQueryChange(it) },
            placeholder = { Text("Search deals...") },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = dimensionResource(R.dimen.small))
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    scope.launch {
                        listState.scrollToItem(0)
                    }
                    keyboardController?.hide()
                    onSearch()
                }
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

        Button(
            onClick = {
                scope.launch {
                    listState.scrollToItem(0)
                }
                keyboardController?.hide()
                onSearch()
            },
            shape = RoundedCornerShape(topEnd = 25.dp, bottomEnd = 25.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White
            )
        }
    }
}