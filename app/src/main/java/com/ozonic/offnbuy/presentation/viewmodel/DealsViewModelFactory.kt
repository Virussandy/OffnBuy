package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ozonic.offnbuy.domain.usecase.GetDealsUseCase

/**
 * Factory for creating instances of [DealsViewModel].
 */
@Suppress("UNCHECKED_CAST")
class DealsViewModelFactory(
    private val getDealsUseCase: GetDealsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DealsViewModel::class.java)) {
            return DealsViewModel(getDealsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}