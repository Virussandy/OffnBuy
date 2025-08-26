package com.ozonic.offnbuy.di

import android.app.Application
import android.content.Context
import com.ozonic.offnbuy.data.local.AppDatabase
import com.ozonic.offnbuy.data.repository.AuthRepositoryImpl
import com.ozonic.offnbuy.data.repository.DealsRepositoryImpl
import com.ozonic.offnbuy.data.repository.LinkRepositoryImpl
import com.ozonic.offnbuy.data.repository.NotificationRepositoryImpl
import com.ozonic.offnbuy.data.repository.SearchRepositoryImpl
import com.ozonic.offnbuy.data.repository.SupportedStoreRepositoryImpl
import com.ozonic.offnbuy.data.repository.UserDataRepositoryImpl
import com.ozonic.offnbuy.domain.repository.AuthRepository
import com.ozonic.offnbuy.domain.repository.DealsRepository
import com.ozonic.offnbuy.domain.repository.LinkRepository
import com.ozonic.offnbuy.domain.repository.NotificationRepository
import com.ozonic.offnbuy.domain.repository.SearchRepository
import com.ozonic.offnbuy.domain.repository.SupportedStoreRepository
import com.ozonic.offnbuy.domain.repository.UserDataRepository
import com.ozonic.offnbuy.util.NetworkConnectivityObserver
import com.ozonic.offnbuy.util.SharedPrefManager

/**
 * Provides dependencies related to the data layer, such as repositories.
 */
object DataModule {

    /**
     * Provides an implementation of the [AuthRepository].
     *
     * @param context The application context.
     * @return An instance of [AuthRepository].
     */
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }

    /**
     * Provides an implementation of the [DealsRepository].
     *
     * @param context The application context.
     * @return An instance of [DealsRepository].
     */
    fun provideDealsRepository(context: Context): DealsRepository {
        val database = AppModule.provideDatabase(context)
        return DealsRepositoryImpl(
            database.dealDao(),
            AppModule.provideNetworkConnectivityObserver(context)
        )
    }

    /**
     * Provides an implementation of the [LinkRepository].
     *
     * @return An instance of [LinkRepository].
     */
    fun provideLinkRepository(context: Context): LinkRepository { // Modify signature
        return LinkRepositoryImpl(AppModule.provideNetworkConnectivityObserver(context)) // Pass observer
    }

    /**
     * Provides an implementation of the [NotificationRepository].
     *
     * @param context The application context.
     * @return An instance of [NotificationRepository].
     */
    fun provideNotificationRepository(context: Context): NotificationRepository {
        val database = AppModule.provideDatabase(context)
        return NotificationRepositoryImpl(
            context.applicationContext as Application,
            database.notifiedDealDao(),
            AppModule.provideNetworkConnectivityObserver(context),
            AppModule.provideSharedPrefManager(context)
        )
    }

    /**
     * Provides an implementation of the [SearchRepository].
     *
     * @return An instance of [SearchRepository].
     */
    fun provideSearchRepository(): SearchRepository {
        return SearchRepositoryImpl()
    }

    /**
     * Provides an implementation of the [SupportedStoreRepository].
     *
     * @param context The application context.
     * @return An instance of [SupportedStoreRepository].
     */
    fun provideSupportedStoreRepository(context: Context): SupportedStoreRepository {
        val database = AppModule.provideDatabase(context)
        return SupportedStoreRepositoryImpl(database.supportedStoreDao())
    }

    /**
     * Provides an implementation of the [UserDataRepository].
     *
     * @param context The application context.
     * @return An instance of [UserDataRepository].
     */
    fun provideUserDataRepository(context: Context): UserDataRepository {
        val database = AppModule.provideDatabase(context)
        return UserDataRepositoryImpl(
            database.favoriteDealDao(),
            database.generatedLinkDao(),
            database.userProfileDao()
        )
    }
}