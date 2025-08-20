package com.ozonic.offnbuy.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DealDetailViewModelFactory(
    private val application: Application,
    private val dealId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DealDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DealDetailViewModel(application, dealId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}