package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.ozonic.offnbuy.domain.model.Deal
import com.ozonic.offnbuy.domain.usecase.GetDealsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the deals screen.
 * This ViewModel handles fetching, displaying, and refreshing the list of deals.
 * It also listens for new deals in real-time to notify the user.
 */
class DealsViewModel(private val getDealsUseCase: GetDealsUseCase) : ViewModel() {

    // Main state flows for the UI
    private val _deals = MutableStateFlow<List<Deal>>(emptyList())
    val deals: StateFlow<List<Deal>> = _deals

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isInitialLoad = MutableStateFlow(true)
    val isInitialLoad: StateFlow<Boolean> = _isInitialLoad

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _hasMoreItems = MutableStateFlow(true)
    val hasMoreItems: StateFlow<Boolean> = _hasMoreItems

    // State flows for real-time updates and refresh logic
    private val _newDealAvailable = MutableStateFlow(false)
    val newDealAvailable: StateFlow<Boolean> = _newDealAvailable

    private val _refreshCount = MutableStateFlow(0)
    val refreshCount: StateFlow<Int> = _refreshCount

    // Internal state for pagination and listeners
    private var lastVisible: DocumentSnapshot? = null
    private var newDealsListener: ListenerRegistration? = null

    init {
        // Initial data load
        loadMoreDeals(isInitial = true)

        // Set up the reactive data stream from the use case
        viewModelScope.launch {
            getDealsUseCase.execute()
                .catch { /* Handle errors if necessary */ }
                .collect { dealList ->
                    _deals.value = dealList
                    // After the first data load, start listening for new deals
                    if (!_isInitialLoad.value) {
                        listenForNewDeals(dealList)
                    }
                }
        }
    }

    /**
     * Listens for new deals in real-time that are newer than the latest one displayed.
     */
    private fun listenForNewDeals(currentDeals: List<Deal>) {
        val latestTimeStamp = currentDeals.firstOrNull()?.posted_on
        if (latestTimeStamp != null) {
            newDealsListener?.remove() // Remove previous listener
            newDealsListener = getDealsUseCase.listenForNewDeals(latestTimeStamp) { newDeal ->
                // Check if the new deal is not already in the list
                if (currentDeals.none { it.deal_id == newDeal.deal_id }) {
                    _newDealAvailable.value = true
                }
            }
        }
    }

    /**
     * Loads the next page of deals from the remote data source.
     */
    fun loadMoreDeals(isInitial: Boolean = false) {
        if (_isLoading.value || !_hasMoreItems.value) return
        if (!isInitial) {
            _isLoading.value = true
        }
        viewModelScope.launch {
            try {
                val next = getDealsUseCase.sync(lastVisible)
                lastVisible = next
                _hasMoreItems.value = next != null
            } catch (e: Exception) {
                // Handle error
            } finally {
                if (isInitial) {
                    _isInitialLoad.value = false
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Refreshes the deals from the remote data source, fetching the newest items.
     */
    fun refreshDeals() {
        if (_isRefreshing.value) return

        _isRefreshing.value = true
        _newDealAvailable.value = false

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                getDealsUseCase.refresh()
                _refreshCount.value++ // Increment to trigger UI events like scrolling
            } catch (e: Exception) {
                // Handle error
            } finally {
                delay(500)
                _isRefreshing.value = false
            }
        }
    }

    override fun onCleared() {
        newDealsListener?.remove()
        super.onCleared()
    }
}