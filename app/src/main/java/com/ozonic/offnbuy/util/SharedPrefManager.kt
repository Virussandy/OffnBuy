package com.ozonic.offnbuy.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class SharedPrefManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_DEAL_IDS = "seen_deal_ids"
        const val IS_FIRST_TIME = "is_first_time_run"
        const val DARK_MODE = "dark_mode"
        const val DYNAMIC_COLOR = "dynamic_color"
    }

    // A generic extension function to convert any preference into a Flow
    private fun <T> SharedPreferences.asFlow(
        key: String,
        getter: SharedPreferences.(String) -> T
    ): Flow<T> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (key == changedKey) {
                trySend(getter(key))
            }
        }
        registerOnSharedPreferenceChangeListener(listener)
        // Emit the initial value when the flow is collected
        trySend(getter(key))
        // Unregister the listener when the flow is closed
        awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // --- Reactive Flows for ViewModel ---
    val darkModeFlow: Flow<Boolean> = sharedPreferences.asFlow(DARK_MODE) {
        getBoolean(it, false)
    }

    val dynamicColorFlow: Flow<Boolean> = sharedPreferences.asFlow(DYNAMIC_COLOR) {
        getBoolean(it, false)
    }

    private val _notificationsEnabled = MutableStateFlow(true)
    fun notificationsEnabledFlow(application: Application): Flow<Boolean> {
        _notificationsEnabled.value = NotificationManagerCompat.from(application).areNotificationsEnabled()
        return _notificationsEnabled
    }
    fun checkNotificationStatus(application: Application) {
        _notificationsEnabled.value = NotificationManagerCompat.from(application).areNotificationsEnabled()
    }


    // --- Standard Getters and Setters ---

    fun getSeenDealsIds(): Set<String> {
        return sharedPreferences.getStringSet(KEY_DEAL_IDS, emptySet()) ?: emptySet()
    }

    fun addSeenDealId(dealId: String) {
        val currentIds = getSeenDealsIds().toMutableSet()
        currentIds.add(dealId)
        sharedPreferences.edit { putStringSet(KEY_DEAL_IDS, currentIds) }
    }

    fun isFirstTimeRun(): Boolean {
        return sharedPreferences.getBoolean(IS_FIRST_TIME, true)
    }

    fun setFirstTimeRun(isFirstTime: Boolean) {
        // Corrected: No .apply() needed inside the block
        sharedPreferences.edit { putBoolean(IS_FIRST_TIME, isFirstTime) }
    }

    fun getDarkMode(): Boolean {
        return sharedPreferences.getBoolean(DARK_MODE, false)
    }

    fun setDarkMode(isDarkMode: Boolean) {
        // Corrected: No .apply() needed inside the block
        sharedPreferences.edit { putBoolean(DARK_MODE, isDarkMode) }
    }

    fun getDynamicColor(): Boolean {
        return sharedPreferences.getBoolean(DYNAMIC_COLOR, false)
    }

    fun setDynamicColor(isDynamicColor: Boolean) {
        // Corrected: No .apply() needed inside the block
        sharedPreferences.edit { putBoolean(DYNAMIC_COLOR, isDynamicColor) }
    }
}