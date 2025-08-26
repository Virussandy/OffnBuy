package com.ozonic.offnbuy.presentation.viewmodel

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.presentation.ui.screens.SettingsState
import com.ozonic.offnbuy.util.SharedPrefManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val application: Application,
    private val authViewModel: AuthViewModel, // Kept for observing user session if needed in future
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    val settingsState: StateFlow<SettingsState> = combine(
        sharedPrefManager.darkModeFlow,
        sharedPrefManager.dynamicColorFlow,
        sharedPrefManager.notificationsEnabledFlow(application)
    ) { isDarkMode, isDynamicColor, notificationsEnabled ->
        SettingsState(
            isDarkMode = isDarkMode,
            isDynamicColor = isDynamicColor,
            notificationEnabled = notificationsEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState(
            isDarkMode = sharedPrefManager.getDarkMode(),
            isDynamicColor = sharedPrefManager.getDynamicColor(),
            notificationEnabled = NotificationManagerCompat.from(application).areNotificationsEnabled()
        )
    )

    fun toggleDarkMode() {
        val currentMode = settingsState.value.isDarkMode
        sharedPrefManager.setDarkMode(!currentMode)
    }

    fun toggleDynamicColor() {
        val currentDynamic = settingsState.value.isDynamicColor
        sharedPrefManager.setDynamicColor(!currentDynamic)
    }

    fun checkNotificationStatus() {
        sharedPrefManager.checkNotificationStatus(application)
    }
}