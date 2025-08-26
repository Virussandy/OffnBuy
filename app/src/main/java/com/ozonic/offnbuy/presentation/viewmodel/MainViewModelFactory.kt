package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ozonic.offnbuy.util.NetworkConnectivityObserver

/**
 * Factory for creating instances of [MainViewModel].
 */
@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(
    private val networkConnectivityObserver: NetworkConnectivityObserver
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(networkConnectivityObserver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}