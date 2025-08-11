package com.ozonic.offnbuy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.model.SearchResultStatus
import com.ozonic.offnbuy.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val repository = SearchRepository()

    private val _deals = MutableStateFlow<List<DealItem>>(emptyList())
    val deals: StateFlow<List<DealItem>> = _deals.asStateFlow()

    private val _searchResultStatus = MutableStateFlow<SearchResultStatus>(SearchResultStatus.Idle)
    val searchResultStatus: StateFlow<SearchResultStatus> = _searchResultStatus.asStateFlow()


    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMoreItems = MutableStateFlow(true)
    val hasMoreItems: StateFlow<Boolean> = _hasMoreItems.asStateFlow()

    private var currentPage = 0 // ✨ Track the current page

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun executeSearch() {
        if (_searchQuery.value.isBlank()) {
            _deals.value = emptyList()
            _hasMoreItems.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _deals.value = emptyList() // Clear previous results
            currentPage = 0 // Reset page number
            try {
                val (newDeals, hasMore) = repository.searchDeals(_searchQuery.value, currentPage)
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

    fun loadMoreDeals() {
        if (_isLoading.value || !_hasMoreItems.value || _searchQuery.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            currentPage++ // Increment for the next page
            try {
                val (newDeals, hasMore) = repository.searchDeals(_searchQuery.value, currentPage)
                if (newDeals.isNotEmpty()) {
                    _deals.value = _deals.value + newDeals
                    _hasMoreItems.value = hasMore
                } else {
                    _hasMoreItems.value = false
                    _searchResultStatus.value = SearchResultStatus.EndReached
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}