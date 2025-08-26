package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ozonic.offnbuy.domain.repository.DealsRepository

/**
 * Factory for creating instances of [DealDetailViewModel].
 */
@Suppress("UNCHECKED_CAST")
class DealDetailViewModelFactory(
    private val dealId: String,
    private val dealsRepository: DealsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DealDetailViewModel::class.java)) {
            return DealDetailViewModel(dealId, dealsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}