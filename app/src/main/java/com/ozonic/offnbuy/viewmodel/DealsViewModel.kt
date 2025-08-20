package com.ozonic.offnbuy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.repository.DealsRepository
import com.ozonic.offnbuy.util.FirebaseInitialization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DealsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DealsRepository((application as FirebaseInitialization).database.dealDao())

    val deals: StateFlow<List<DealItem>> = repository.getDealsStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isInitialLoad = MutableStateFlow(true)
    val isInitialLoad: StateFlow<Boolean> = _isInitialLoad
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _hasMoreItems = MutableStateFlow(true)
    val hasMoreItems: StateFlow<Boolean> = _hasMoreItems

    private var lastVisibleSnapshot: DocumentSnapshot? = null

    private var newDealsListener: ListenerRegistration? = null

    private val _newDealAvailable = MutableStateFlow(false)
    val newDealAvailable: StateFlow<Boolean> = _newDealAvailable

    private val _refreshCount = MutableStateFlow(0)
    val refreshCount: StateFlow<Int> = _refreshCount

    init {
        // This starts the initial asynchronous load.
        loadMoreDeals(isInitial = true)

        viewModelScope.launch {
            // This flow will now only proceed when `isInitialLoad` becomes false.
            combine(deals, isInitialLoad) { currentDeals, isInitial ->
                Pair(currentDeals, isInitial)
            }.collectLatest { (currentDeals, isInitial) ->
                // Only attach the real-time listener AFTER the initial load is complete.
                if (!isInitial) {
                    val latestTimeStamp = currentDeals.firstOrNull()?.posted_on
                    if (latestTimeStamp != null) {
                        newDealsListener?.remove()
                        newDealsListener = repository.listenForNewDeals(latestTimeStamp) { newDeal ->
                            if (currentDeals.none { it.deal_id == newDeal.deal_id }) {
                                _newDealAvailable.value = true
                            }
                        }
                    }
                }
            }
        }
    }

    fun loadMoreDeals(isInitial: Boolean = false) {
        if (_isLoading.value || !_hasMoreItems.value) return
        if(!isInitial){
            _isLoading.value = true
        }
        viewModelScope.launch {
            try {
                val nextSnapshot = repository.syncDeals(lastVisibleSnapshot)
                lastVisibleSnapshot = nextSnapshot
                _hasMoreItems.value = nextSnapshot != null
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if(isInitial){
                    _isInitialLoad.value = false
                }
                _isLoading.value = false
            }
        }
    }

        fun refreshDeals() {
            if (_isRefreshing.value) return

            _isRefreshing.value = true
            _newDealAvailable.value = false

            viewModelScope.launch {
                try {
                  repository.syncNewestDeals()
                    _refreshCount.value++
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isRefreshing.value = false
                }
            }
        }

    override fun onCleared() {
        newDealsListener?.remove()
        super.onCleared()
    }
}
