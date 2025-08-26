package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.domain.model.UserProfile
import com.ozonic.offnbuy.domain.usecase.GetUserProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the profile screen.
 */
class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    /**
     * Gets the user profile for the given user ID.
     *
     * @param userId The ID of the user to get the profile for.
     */
    fun getUserProfile(userId: String) {
        viewModelScope.launch {
            getUserProfileUseCase.execute(userId)
                .catch {
                    // Handle error
                }
                .collect {
                    _userProfile.value = it
                }
        }
    }
}