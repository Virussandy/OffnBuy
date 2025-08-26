package com.ozonic.offnbuy.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ozonic.offnbuy.di.DataModule

@Suppress("UNCHECKED_CAST")
class LinkViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LinkViewModel::class.java)) {
            return LinkViewModel(
                linkRepository = DataModule.provideLinkRepository(context),
                userDataRepository = DataModule.provideUserDataRepository(context),
                supportedStoreRepository = DataModule.provideSupportedStoreRepository(context),
                // Add the AuthRepository to observe the user
                authRepository = DataModule.provideAuthRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}