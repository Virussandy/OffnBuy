package com.ozonic.offnbuy.di

import android.content.Context
import com.ozonic.offnbuy.domain.usecase.GetDealsUseCase
import com.ozonic.offnbuy.domain.usecase.GetNotificationsUseCase
import com.ozonic.offnbuy.domain.usecase.GetUserProfileUseCase
import com.ozonic.offnbuy.domain.usecase.SearchDealsUseCase

/**
 * Provides dependencies for the ViewModels.
 */
object ViewModelModule {

    /**
     * Provides the [GetDealsUseCase].
     *
     * @param context The application context.
     * @return An instance of [GetDealsUseCase].
     */
    fun provideGetDealsUseCase(context: Context): GetDealsUseCase {
        return GetDealsUseCase(DataModule.provideDealsRepository(context))
    }

    /**
     * Provides the [GetNotificationsUseCase].
     *
     * @param context The application context.
     * @return An instance of [GetNotificationsUseCase].
     */
    fun provideGetNotificationsUseCase(context: Context): GetNotificationsUseCase {
        return GetNotificationsUseCase(DataModule.provideNotificationRepository(context))
    }

    /**
     * Provides the [GetUserProfileUseCase].
     *
     * @param context The application context.
     * @return An instance of [GetUserProfileUseCase].
     */
    fun provideGetUserProfileUseCase(context: Context): GetUserProfileUseCase {
        return GetUserProfileUseCase(DataModule.provideUserDataRepository(context))
    }


    /**
     * Provides the [SearchDealsUseCase].
     *
     * @return An instance of [SearchDealsUseCase].
     */
    fun provideSearchDealsUseCase(): SearchDealsUseCase {
        return SearchDealsUseCase(DataModule.provideSearchRepository())
    }
}