package com.ozonic.offnbuy.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ozonic.offnbuy.util.SharedPrefManager

/**
 * Factory for creating instances of [SettingsViewModel].
 */
@Suppress("UNCHECKED_CAST")
class SettingsViewModelFactory(
    private val application: Application,
    private val authViewModel: AuthViewModel,
    private val sharedPrefManager: SharedPrefManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(application, authViewModel, sharedPrefManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}