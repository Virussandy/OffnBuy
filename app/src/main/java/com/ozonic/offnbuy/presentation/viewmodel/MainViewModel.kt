package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.util.NetworkConnectivityObserver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the main activity/screen.
 * This ViewModel holds global state relevant to the entire application,
 * such as the network connectivity status.
 *
 * @param connectivityObserver The observer that provides a flow of network status changes.
 */
class MainViewModel(
    private val connectivityObserver: NetworkConnectivityObserver
) : ViewModel() {

    /**
     * A StateFlow that emits the current network connectivity status.
     * The UI can collect this flow to reactively show or hide an "offline" banner.
     */
    val isOnline: StateFlow<Boolean> = connectivityObserver.observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true // Assume online initially to avoid a flicker on app start
        )
}