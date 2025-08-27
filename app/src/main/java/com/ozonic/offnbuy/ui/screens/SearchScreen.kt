package com.ozonic.offnbuy.presentation.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.domain.model.SearchResultStatus
import com.ozonic.offnbuy.presentation.viewmodel.SearchViewModel
import com.ozonic.offnbuy.ui.screens.DealCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, viewModel: SearchViewModel) {
    val context = LocalContext.current

    val searchDeals by viewModel.deals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val hasMoreItems by viewModel.hasMoreItems.collectAsState()
    val searchStatus by viewModel.searchResultStatus.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val showSuggestions by viewModel.showSuggestions.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

//    LaunchedEffect(Unit) {
//        if (searchDeals.isNotEmpty()) {
//            listState.scrollToItem(0)
//        }
//    }

    AppScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Search",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background, // Set this to the same color
                ),
            )
        },
        navController = navController,
        showBottomNav = true,
    ) { paddingValues ->
        Box (modifier = Modifier.fillMaxSize().padding(paddingValues)){
            Column(modifier = Modifier
                .fillMaxSize()) {
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = {
                        focusManager.clearFocus()
                        viewModel.executeSearch() },
//                    listState = listState,
                    onFocusChanged = { viewModel.onFocusChanged(it) },
                    onClear = { viewModel.onClearSearch() },
                    focusRequester = focusRequester,
                    //                scope = scope,
                )
                if (searchStatus == SearchResultStatus.Idle && !isLoading) {
                    InitialSearchPrompt()
                }else{
                    LazyVerticalGrid(
                        state = listState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
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
                                    viewModel.loadMoreDeals()
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
            if (showSuggestions && suggestions.isNotEmpty()) {
                SearchSuggestions(
                    suggestions = suggestions,
                    onSuggestionClick = { suggestion ->
                        viewModel.onSearchQueryChange(suggestion) // Optional: update TextField
                        focusManager.clearFocus()
                        viewModel.executeSearch(suggestion)
                    },
                    modifier = Modifier.padding(top = 80.dp) // Position it below the SearchBar
                )
            }
        }
    }
}

@Composable
fun SearchSuggestions(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        LazyColumn {
            items(suggestions) { suggestion ->
                ListItem(
                    headlineContent = { Text(suggestion) },
                    modifier = Modifier.clickable { onSuggestionClick(suggestion) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
//    listState: LazyGridState,
    onFocusChanged: (Boolean) -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester,
//    scope: CoroutineScope,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
                .focusRequester(focusRequester)
                .onFocusChanged { focusState -> onFocusChanged(focusState.isFocused) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
//                    scope.launch {
//                        listState.scrollToItem(0)
//                    }
                    keyboardController?.hide()
                    onSearch()
                }
            ),
            leadingIcon = {
                if(searchQuery.isNotEmpty()){
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Button(
            onClick = {
//                scope.launch {
//                    listState.scrollToItem(0)
//                }
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

@Composable
fun InitialSearchPrompt() {
    Column(
        modifier = Modifier
            .fillMaxSize()
//            .padding(top = 64.dp)
            ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Find Your Next Deal",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Start typing to search for products and stores.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}