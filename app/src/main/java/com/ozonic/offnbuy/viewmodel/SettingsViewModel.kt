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

    private val _dynamicColor = MutableStateFlow<Boolean>(false)
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow<Boolean>(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    init {
        _darkMode.value = sharedPrefManager.getDarkMode()
        _dynamicColor.value = sharedPrefManager.getDynamicColor()
        checkNotificationStatus(application)
    }

    fun toggleDarkMode() {
        _darkMode.value = !_darkMode.value
        sharedPrefManager.setDarkMode(_darkMode.value)
    }

    fun toggleDynamicColor() {
        _dynamicColor.value = !_dynamicColor.value
        sharedPrefManager.setDynamicColor(_dynamicColor.value)

    }

    fun checkNotificationStatus(application: Application){
        _notificationsEnabled.value = NotificationManagerCompat.from(application).areNotificationsEnabled()
    }

}