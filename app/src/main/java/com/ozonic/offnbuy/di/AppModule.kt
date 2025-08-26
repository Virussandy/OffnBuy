package com.ozonic.offnbuy.di

import android.content.Context
import com.ozonic.offnbuy.data.local.AppDatabase
import com.ozonic.offnbuy.util.NetworkConnectivityObserver
import com.ozonic.offnbuy.util.SharedPrefManager

/**
 * Provides application-level dependencies.
 */
object AppModule {

    /**
     * Provides a singleton instance of the Room database.
     *
     * @param context The application context.
     * @return An instance of [AppDatabase].
     */
    fun provideDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    /**
     * Provides a singleton instance of the NetworkConnectivityObserver.
     *
     * @param context The application context.
     * @return An instance of [NetworkConnectivityObserver].
     */
    fun provideNetworkConnectivityObserver(context: Context): NetworkConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }

    /**
     * Provides a singleton instance of the SharedPrefManager.
     *
     * @param context The application context.
     * @return An instance of [SharedPrefManager].
     */
    fun provideSharedPrefManager(context: Context): SharedPrefManager {
        return SharedPrefManager(context)
    }
}