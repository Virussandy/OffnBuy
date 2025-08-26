package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ozonic.offnbuy.domain.usecase.GetNotificationsUseCase
import com.ozonic.offnbuy.util.SharedPrefManager

/**
 * Factory for creating instances of [NotificationViewModel].
 */
@Suppress("UNCHECKED_CAST")
class NotificationViewModelFactory(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val sharedPrefManager: SharedPrefManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            return NotificationViewModel(getNotificationsUseCase, sharedPrefManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}