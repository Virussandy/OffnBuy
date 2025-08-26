package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ozonic.offnbuy.domain.usecase.SearchDealsUseCase

/**
 * Factory for creating instances of [SearchViewModel].
 */
@Suppress("UNCHECKED_CAST")
class SearchViewModelFactory(
    private val searchDealsUseCase: SearchDealsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(searchDealsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}