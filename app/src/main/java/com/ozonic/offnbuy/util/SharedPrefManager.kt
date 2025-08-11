package com.ozonic.offnbuy.util

import android.content.Context
import androidx.core.content.edit

class SharedPrefManager(context: Context){
    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object{
        const val KEY_DEAL_IDS = "seen_deal_ids"
        const val IS_FIRST_TIME = "is_first_time_run"
        const val DARK_MODE = "dark_mode"
    }

    fun getSeenDealsIds() : Set<String>{
        return sharedPreferences.getStringSet(KEY_DEAL_IDS, emptySet()) ?: emptySet()
    }

    fun addSeenDealId(dealId: String){
        val currentIds = getSeenDealsIds().toMutableSet()
        currentIds.add(dealId)
        sharedPreferences.edit { putStringSet(KEY_DEAL_IDS, currentIds) }
    }

    fun isFirstTimeRun() : Boolean{
        return sharedPreferences.getBoolean(IS_FIRST_TIME, true)
    }

    fun setFirstTimeRun(isFirstTime: Boolean){
        sharedPreferences.edit { putBoolean(IS_FIRST_TIME, isFirstTime).apply() }
    }

    fun getDarkMode(): Boolean {
        return sharedPreferences.getBoolean(DARK_MODE, false)
    }

    fun setDarkMode(isDarkMode: Boolean) {
        sharedPreferences.edit { putBoolean(DARK_MODE, isDarkMode) }
    }
}