package com.ozonic.offnbuy.presentation.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ozonic.offnbuy.MainActivity
import com.ozonic.offnbuy.di.AppModule
import com.ozonic.offnbuy.di.DataModule
import com.ozonic.offnbuy.di.ViewModelModule
import com.ozonic.offnbuy.domain.model.ContentType
import com.ozonic.offnbuy.domain.model.NavigationItem
import com.ozonic.offnbuy.presentation.ui.screens.*
import com.ozonic.offnbuy.presentation.viewmodel.*
import com.ozonic.offnbuy.util.InAppUpdateManager
import com.ozonic.offnbuy.util.NotificationSyncManager
import com.ozonic.offnbuy.util.UserDataManager
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val activity = context as MainActivity

    val dealsViewModel: DealsViewModel = viewModel(factory = DealsViewModelFactory(ViewModelModule.provideGetDealsUseCase(context)))

    // ✅ Initialize your managers here, using the single AuthViewModel instance
    // and the Activity's lifecycleScope for long-running jobs.
    LaunchedEffect(key1 = Unit) {
        val userDataRepository = DataModule.provideUserDataRepository(application)
        val notificationRepository = DataModule.provideNotificationRepository(application)

        val userDataManager =
            UserDataManager(activity.lifecycleScope, authViewModel, userDataRepository)
        val notificationSyncManager = NotificationSyncManager(notificationRepository)

        userDataManager.start()
        notificationSyncManager.start()
    }

    NavHost(
        navController = navHostController,
        startDestination = NavigationItem.Home.route,
        modifier = modifier
    ) {
        composable(NavigationItem.Home.route) {
            DealsScreen(navController = navHostController, viewModel = dealsViewModel)
        }
        composable(NavigationItem.Search.route) {
            val searchViewModel: SearchViewModel = viewModel(
                factory = SearchViewModelFactory(ViewModelModule.provideSearchDealsUseCase())
            )
            SearchScreen(navController = navHostController, viewModel = searchViewModel)
        }
        composable(NavigationItem.Notifications.route) {
            NotificationScreen(navController = navHostController)
        }
        composable(NavigationItem.Profile.route) {
            ProfileScreen(
                navController = navHostController,
                authViewModel = authViewModel,
                settingsViewModel = settingsViewModel,
            )
        }
        composable(NavigationItem.Links.route) {
            GenerateLinkScreen(navController = navHostController)
        }
        composable(
            route = NavigationItem.DealDetailScreen.route,
            arguments = NavigationItem.DealDetailScreen.arguments
        ) { backStackEntry ->
            val dealIdArg = backStackEntry.arguments?.getString("dealId")
            if (dealIdArg != null) {
                DealDetailScreen(dealId = dealIdArg, navController = navHostController)
            }
        }
        composable(
            route = NavigationItem.ContentScreen.route,
            arguments = NavigationItem.ContentScreen.arguments
        ) { backStackEntry ->
            val contentTypeArg = backStackEntry.arguments?.getString("contentType")
            val contentType = when (contentTypeArg) {
                ContentType.TermsAndConditions.route -> ContentType.TermsAndConditions
                ContentType.PrivacyPolicy.route -> ContentType.PrivacyPolicy
                else -> ContentType.HelpAndSupport
            }
            ContentScreen(contentType = contentType, navController = navHostController)
        }
        composable(NavigationItem.AuthScreen.route) {
            AuthScreen(
                navController = navHostController,
                authViewModel = authViewModel,
                onAuthComplete = { navHostController.popBackStack() }
            )
        }
        composable(NavigationItem.EditProfileScreen.route) {
            EditProfileScreen(navController = navHostController, authViewModel = authViewModel)
        }
    }
}