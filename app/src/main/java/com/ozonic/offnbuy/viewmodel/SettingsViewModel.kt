package com.ozonic.offnbuy.viewmodel

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import com.ozonic.offnbuy.util.SharedPrefManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : ViewModel() {

    private val sharedPrefManager: SharedPrefManager = SharedPrefManager(application)
    private val _darkMode = MutableStateFlow<Boolean>(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow<Boolean>(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    init {
        _darkMode.value = sharedPrefManager.getDarkMode()
        checkNotificationStatus(application)
    }

    fun toggleDarkMode() {
        _darkMode.value = !_darkMode.value
        sharedPrefManager.setDarkMode(_darkMode.value)
    }

    fun checkNotificationStatus(application: Application){
        _notificationsEnabled.value = androidx.core.app.NotificationManagerCompat.from(application).areNotificationsEnabled()
    }

}