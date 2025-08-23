package com.ozonic.offnbuy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.data.UserProfileDao
import com.ozonic.offnbuy.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    authViewModel: AuthViewModel,
    userProfileDao: UserProfileDao
) : ViewModel() {

    // This StateFlow will ALWAYS have the latest profile data from the local DB.
    val userProfile: StateFlow<UserProfile?> = authViewModel.authState.flatMapLatest { authState ->
        if (authState is AuthState.Authenticated && !authState.user.isAnonymous) {
            // If we have a logged-in user, observe their profile from the database
            userProfileDao.getProfile(authState.user.uid)
        } else {
            // If no user or anonymous, provide a null profile
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}