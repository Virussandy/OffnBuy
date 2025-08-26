package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ozonic.offnbuy.domain.repository.LinkRepository

/**
 * Factory for creating instances of [ShareViewModel].
 * This is required because the ViewModel has a constructor dependency on [LinkRepository].
 *
 * @param linkRepository The repository for generating links.
 */
@Suppress("UNCHECKED_CAST")
class ShareViewModelFactory(
    private val linkRepository: LinkRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShareViewModel::class.java)) {
            return ShareViewModel(linkRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}