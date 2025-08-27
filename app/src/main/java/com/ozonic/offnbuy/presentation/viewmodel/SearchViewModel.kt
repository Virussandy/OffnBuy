package com.ozonic.offnbuy.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.domain.model.Deal
import com.ozonic.offnbuy.domain.model.SearchResultStatus
import com.ozonic.offnbuy.domain.usecase.SearchDealsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchDealsUseCase: SearchDealsUseCase
) : ViewModel() {

    private val _deals = MutableStateFlow<List<Deal>>(emptyList())
    val deals: StateFlow<List<Deal>> = _deals.asStateFlow()

    private val _searchResultStatus = MutableStateFlow<SearchResultStatus>(SearchResultStatus.Idle)
    val searchResultStatus: StateFlow<SearchResultStatus> = _searchResultStatus.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // NEW: State for suggestions
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    // NEW: State to control suggestion box visibility
    private val _isFocused = MutableStateFlow(false)
    val showSuggestions: StateFlow<Boolean> = combine(_isFocused, _searchQuery) { isFocused, query ->
        isFocused && query.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = false
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMoreItems = MutableStateFlow(true)
    val hasMoreItems: StateFlow<Boolean> = _hasMoreItems.asStateFlow()

    private var currentPage = 0

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.length > 1) { // Only fetch suggestions if query is > 1 char
                        Log.d("SearchViewModel", "Fetching suggestions for query: $query")
                        fetchSuggestions(query)
                    } else {
                        _suggestions.value = emptyList()
                    }
                }
        }
    }

    private suspend fun fetchSuggestions(query: String) {
        val newSuggestions = searchDealsUseCase.getSuggestions(query)
        Log.d("SearchViewModel", "Fetched suggestions: $newSuggestions")
        _suggestions.value = newSuggestions
    }

    private suspend fun performSearch(query: String) {
        if (query.isBlank()) return
        _isLoading.value = true
        _deals.value = emptyList()
        currentPage = 0
        try {
            val (newDeals, hasMore) = searchDealsUseCase.execute(query, currentPage)
            _deals.value = newDeals
            _hasMoreItems.value = hasMore
            _searchResultStatus.value = if (newDeals.isEmpty()) SearchResultStatus.NoResults else SearchResultStatus.ResultsFound
        } catch (e: Exception) {
            _searchResultStatus.value = SearchResultStatus.NoResults
        } finally {
            _isLoading.value = false
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun executeSearch(query: String = _searchQuery.value) {
        _searchQuery.value = query // Update the query in the search bar
        _suggestions.value = emptyList() // Clear suggestions
        viewModelScope.launch {
            performSearch(query)
        }
    }

    fun onFocusChanged(isFocused: Boolean) {
        _isFocused.value = isFocused
    }

    fun onClearSearch() {
        _searchQuery.value = ""
        _suggestions.value = emptyList()
        _deals.value = emptyList()
        _searchResultStatus.value = SearchResultStatus.Idle
        }

    /**
     * Loads the next page of search results for the current query.
     */
    fun loadMoreDeals() {
        if (_isLoading.value || !_hasMoreItems.value || _searchQuery.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            currentPage++ // Increment for the next page
            try {
                val (newDeals, hasMore) = searchDealsUseCase.execute(_searchQuery.value, currentPage)
                if (newDeals.isNotEmpty()) {
                    _deals.value = _deals.value + newDeals
                    _hasMoreItems.value = hasMore
                } else {
                    _hasMoreItems.value = false
                    _searchResultStatus.value = SearchResultStatus.EndReached
                }
            } catch (e: Exception) {
                // Handle error, e.g., show a toast
            } finally {
                _isLoading.value = false
            }
        }
    }
}