package com.ozonic.offnbuy.navigation

import android.app.Application
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ozonic.offnbuy.model.ContentType
import com.ozonic.offnbuy.model.NavigationItem
import com.ozonic.offnbuy.ui.screens.AuthScreen
import com.ozonic.offnbuy.ui.screens.ContentScreen
import com.ozonic.offnbuy.ui.screens.DealDetailScreen
import com.ozonic.offnbuy.ui.screens.DealsScreen
import com.ozonic.offnbuy.ui.screens.EditProfileScreen
import com.ozonic.offnbuy.ui.screens.GenerateLink
import com.ozonic.offnbuy.ui.screens.NotificationScreen
import com.ozonic.offnbuy.ui.screens.ProfileScreen
import com.ozonic.offnbuy.ui.screens.SearchScreen
import com.ozonic.offnbuy.ui.screens.goToNotificationSettings
import com.ozonic.offnbuy.ui.screens.goToStore
import com.ozonic.offnbuy.util.appShare
import com.ozonic.offnbuy.viewmodel.AuthViewModel
import com.ozonic.offnbuy.viewmodel.DealsViewModel
import com.ozonic.offnbuy.viewmodel.GenerateLinkViewModel
import com.ozonic.offnbuy.viewmodel.GenerateLinkViewModelFactory
import com.ozonic.offnbuy.viewmodel.NotificationViewModel
import com.ozonic.offnbuy.viewmodel.ProfileViewModel
import com.ozonic.offnbuy.viewmodel.ProfileViewModelFactory
import com.ozonic.offnbuy.viewmodel.SearchViewModel
import com.ozonic.offnbuy.viewmodel.SettingsViewModel
import java.util.regex.Pattern

@Composable
fun AppNavigation(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
    dealsViewModel: DealsViewModel,
    notificationViewModel: NotificationViewModel,
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    gridState: LazyGridState,
) {

    val context = LocalContext.current
    val application = context.applicationContext as Application

    NavHost(
        navController = navHostController,
        startDestination = NavigationItem.Home.route,
        modifier = modifier
    ) {
        composable(NavigationItem.Home.route) {
            //Deals parameters
            val deals by dealsViewModel.deals.collectAsState()
            val isInitialLoad by dealsViewModel.isInitialLoad.collectAsState()
            val isDealsLoading by dealsViewModel.isLoading.collectAsState()
            val isRefreshing by dealsViewModel.isRefreshing.collectAsState()
            val hasMoreDealItems by dealsViewModel.hasMoreItems.collectAsState()

            DealsScreen(
                deals = deals,
                isLoading = isDealsLoading,
                isRefreshing = isRefreshing,
                isInitialLoad = isInitialLoad,
                hasMoreItems = hasMoreDealItems,
                loadMoreDeals = { dealsViewModel.loadMoreDeals() },
                refreshDeals = { dealsViewModel.refreshDeals() },
                gridState = gridState,
            )
        }
        composable(NavigationItem.Search.route) {

            //Search parameters
            val searchViewModel: SearchViewModel = viewModel()
            val searchDeals by searchViewModel.deals.collectAsState()
            val isSearchLoading by searchViewModel.isLoading.collectAsState()
            val searchQuery by searchViewModel.searchQuery.collectAsState()
            val hasMoreSearchItems by searchViewModel.hasMoreItems.collectAsState()
            val searchStatus by searchViewModel.searchResultStatus.collectAsState()

            SearchScreen(
                searchDeals = searchDeals,
                isLoading = isSearchLoading,
                searchQuery = searchQuery,
                hasMoreItems = hasMoreSearchItems,
                searchStatus = searchStatus,
                onSearch = {
                    searchViewModel.executeSearch()
                },
                onSearchQueryChange = { it ->
                    searchViewModel.onSearchQueryChange(it)
                },
                loadMoreDeals = { searchViewModel.loadMoreDeals() },)
        }
        composable(NavigationItem.Notifications.route) {
//            val isLoading by notificationViewModel.isLoading.collectAsState()
//            val hasMore by notificationViewModel.hasMoreNotifications.collectAsState()
            NotificationScreen(
                viewModel = notificationViewModel,
                navController = navHostController,
                settingsViewModel = settingsViewModel,
//                onLoadMore = { notificationViewModel.loadMoreNotifications() },
//                hasMore = hasMore,
//                isLoading = isLoading
            )
        }
        composable(NavigationItem.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(application, authViewModel)
            )
            val settingsState by settingsViewModel.settingsState.collectAsState()

            ProfileScreen(
                settingsState = settingsState,
                profileViewModel = profileViewModel,
                onToggleDarkMode = { settingsViewModel.toggleDarkMode() },
                onToggleDynamicColor = { settingsViewModel.toggleDynamicColor() },
                onContentScreenClick = { route ->
                    navHostController.navigate(NavigationItem.ContentScreen.withArgs(route))
                },
                onAppShare = { appShare(context) },
                onNotificationSettingsClick = { goToNotificationSettings(context) },
                onLoginClick = { navHostController.navigate(NavigationItem.AuthScreen.route) },
                onEditProfileClick = { navHostController.navigate(NavigationItem.EditProfileScreen.route) },
                onLinkGenerateClick = { navHostController.navigate(NavigationItem.Links.route) },
                onLogout = {
                    authViewModel.logout()
                }
            )
        }

        composable(route = NavigationItem.AuthScreen.route) {
            AuthScreen(
                navController = navHostController,
                authViewModel = authViewModel,
                onAuthComplete = { navHostController.popBackStack() }
            )
        }

        composable(route = NavigationItem.EditProfileScreen.route) {
            EditProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navHostController.popBackStack() }
            )
        }

        composable(
            route = NavigationItem.DealDetailScreen.route,
            arguments = NavigationItem.DealDetailScreen.arguments
        ) { backStackEntry ->
            val dealIdArg = backStackEntry.arguments?.getString("dealId")
            if (dealIdArg != null) {
                DealDetailScreen(dealId = dealIdArg, navController = navHostController)
            } else {
                // Handle error case where dealId is null
                Text("Error: Deal ID not found.")
            }
        }

        composable(
            route = NavigationItem.ContentScreen.route,
            arguments = NavigationItem.ContentScreen.arguments
        ) { backStackEntry ->
            val contentTypeArg = backStackEntry.arguments?.getString("contentType")
            when (contentTypeArg) {
                ContentType.TermsAndConditions.route -> ContentScreen(contentType = ContentType.TermsAndConditions)
                ContentType.PrivacyPolicy.route -> ContentScreen(contentType = ContentType.PrivacyPolicy)
                ContentType.HelpAndSupport.route -> ContentScreen(contentType = ContentType.HelpAndSupport)
            }
        }

        composable(route = NavigationItem.Links.route) {
            //GenerateLink parameters
            val generateLinkViewModel: GenerateLinkViewModel = viewModel(
                factory = GenerateLinkViewModelFactory(
                    application = application,
                    authViewModel = authViewModel
                )
            )
            val productLink by generateLinkViewModel.productLink.collectAsState()
            val recentLinks by generateLinkViewModel.recentLinks.collectAsState()
            val generatedLink by generateLinkViewModel.generatedLink.collectAsState()
            val isLoading by generateLinkViewModel.isLoading.collectAsState()
            val supportedStores by generateLinkViewModel.supportedStores.collectAsState()
            val isError by generateLinkViewModel.isError.collectAsState()
            val clipboardManager = LocalClipboardManager.current

            GenerateLink(
                productLink = productLink,
                onProductLinkChange = { it -> generateLinkViewModel.onProductLinkChange(it) },
                recentLinks = recentLinks,
                listLinkClick = { it -> goToStore(context = context, dealUrl = it) },
                clipboardPaste = {
                    val clipText = clipboardManager.getText()?.text ?: ""
                    val url = extractUrl(clipText)
                    generateLinkViewModel.onProductLinkChange(url ?: clipText)
                },
                onGenerateLink = {
                    generateLinkViewModel.generateLink()
                },
                supportedStores = supportedStores,
                clearProductLink = {generateLinkViewModel.clearProductLink()},
                onDismissDialog = { generateLinkViewModel.clearGeneratedLink() },
                isLoading = isLoading,
                generatedLink = generatedLink,
                isError = isError,
            )
        }
    }
}

private fun extractUrl(text: String): String? {
    val pattern = Pattern.compile("(https?://\\S+)")
    val matcher = pattern.matcher(text)
    return if (matcher.find()) {
        matcher.group(0)
    } else {
        null
    }
}