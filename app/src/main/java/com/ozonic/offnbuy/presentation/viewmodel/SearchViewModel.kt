package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.domain.model.Deal
import com.ozonic.offnbuy.domain.model.SearchResultStatus
import com.ozonic.offnbuy.domain.usecase.SearchDealsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Search screen.
 * This ViewModel handles the logic for searching deals, managing search queries,
 * loading states, pagination, and the overall state of the search results.
 *
 * @param searchDealsUseCase The use case for executing deal searches.
 */
class SearchViewModel(
    private val searchDealsUseCase: SearchDealsUseCase
) : ViewModel() {

    private val _deals = MutableStateFlow<List<Deal>>(emptyList())
    val deals: StateFlow<List<Deal>> = _deals.asStateFlow()

    private val _searchResultStatus = MutableStateFlow<SearchResultStatus>(SearchResultStatus.Idle)
    val searchResultStatus: StateFlow<SearchResultStatus> = _searchResultStatus.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMoreItems = MutableStateFlow(true)
    val hasMoreItems: StateFlow<Boolean> = _hasMoreItems.asStateFlow()

    private var currentPage = 0

    /**
     * Updates the search query text.
     *
     * @param query The new search query string.
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Executes a new search. It resets the previous results and pagination.
     */
    fun executeSearch() {
        if (_searchQuery.value.isBlank()) {
            _deals.value = emptyList()
            _hasMoreItems.value = false
            _searchResultStatus.value = SearchResultStatus.Idle
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _deals.value = emptyList() // Clear previous results
            currentPage = 0 // Reset page number
            try {
                val (newDeals, hasMore) = searchDealsUseCase.execute(_searchQuery.value, currentPage)
                _deals.value = newDeals
                _hasMoreItems.value = hasMore
                _searchResultStatus.value = if (newDeals.isEmpty()) SearchResultStatus.NoResults else SearchResultStatus.ResultsFound
            } catch (e: Exception) {
                _searchResultStatus.value = SearchResultStatus.NoResults
            } finally {
                _isLoading.value = false
            }
        }
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