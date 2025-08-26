package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ozonic.offnbuy.domain.usecase.GetUserProfileUseCase

/**
 * Factory for creating instances of [ProfileViewModel].
 */
@Suppress("UNCHECKED_CAST")
class ProfileViewModelFactory(
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(getUserProfileUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}