package com.ozonic.offnbuy.viewmodel

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.ui.screens.SettingsState
import com.ozonic.offnbuy.util.SharedPrefManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    application: Application,
    authViewModel: AuthViewModel
) : ViewModel() {

    private val sharedPrefManager: SharedPrefManager = SharedPrefManager(application)

    val settingsState: StateFlow<SettingsState> = combine(
        authViewModel.authState,
        sharedPrefManager.darkModeFlow,
        sharedPrefManager.dynamicColorFlow,
        sharedPrefManager.notificationsEnabledFlow(application)
    ) { authState, isDarkMode, isDynamicColor, notificationsEnabled ->
        SettingsState(
            authState = authState,
            isDarkMode = isDarkMode,
            isDynamicColor = isDynamicColor,
            notificationEnabled = notificationsEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState(
            authState = authViewModel.authState.value,
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

    fun checkNotificationStatus(application: Application) {
        sharedPrefManager.checkNotificationStatus(application)
    }
}