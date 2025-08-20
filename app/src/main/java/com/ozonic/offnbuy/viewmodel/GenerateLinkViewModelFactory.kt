package com.ozonic.offnbuy.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GenerateLinkViewModelFactory(
    private val application: Application,
    private val authViewModel: AuthViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GenerateLinkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GenerateLinkViewModel(application, authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}