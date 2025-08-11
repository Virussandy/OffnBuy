package com.ozonic.offnbuy.ui.screens

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ozonic.offnbuy.model.ContentType
import com.ozonic.offnbuy.model.NavigationItem
import com.ozonic.offnbuy.navigation.BottomNavBar
import com.ozonic.offnbuy.ui.theme.OffnBuyTheme
import com.ozonic.offnbuy.viewmodel.DealsViewModel
import com.ozonic.offnbuy.viewmodel.GenerateLinkViewModel
import com.ozonic.offnbuy.viewmodel.NotificationViewModel
import com.ozonic.offnbuy.viewmodel.SearchViewModel
import com.ozonic.offnbuy.viewmodel.SettingsViewModel


@Composable
fun MainScreen(
    dealIdState: MutableState<String?>,
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    var showBottomBar by remember { mutableStateOf(true) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    showBottomBar = when(navBackStackEntry?.destination?.route){
        NavigationItem.ContentScreen.route -> false
        NavigationItem.Search.route -> false
        NavigationItem.DealDetailScreen.route -> false
        NavigationItem.LinkGenerateScreen.route -> false
        else -> true
    }


    //Notification parameters
    var dealId = dealIdState.value
    val notificationViewModel =
        remember { NotificationViewModel(context.applicationContext as Application) }

    //Settings parameters
    val settingsViewModel =
        remember { SettingsViewModel(context.applicationContext as Application) }
    val isDarkMode by settingsViewModel.darkMode.collectAsState()
    val notificationEnabled by settingsViewModel.notificationsEnabled.collectAsState()

    LaunchedEffect(dealId) {
        dealId?.let {
            notificationViewModel.markAsSeen(it)
            navController.navigate(NavigationItem.DealDetailScreen.withArgs(it))
            dealIdState.value = null
        }
    }

    OffnBuyTheme(
        darkTheme = isDarkMode,
    ) {
        Scaffold(
            modifier = Modifier,
            bottomBar = {
                if(showBottomBar){
                    BottomNavBar(
                        navController = navController,
                        notificationViewModel = notificationViewModel
                    )
                }
            },
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = NavigationItem.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(NavigationItem.Home.route) {
                    //Deals parameters
                    val dealsViewModel: DealsViewModel = viewModel()
                    val deals by dealsViewModel.deals.collectAsState()
                    val isInitialLoad by dealsViewModel.isInitialLoad.collectAsState()
                    val newAvailable by dealsViewModel.newDealAvailable.collectAsState()
                    val isDealsLoading by dealsViewModel.isLoading.collectAsState()
                    val isRefreshing by dealsViewModel.isRefreshing.collectAsState()
                    val hasMoreDealItems by dealsViewModel.hasMoreItems.collectAsState()
                    val refreshCount by dealsViewModel.refreshCount.collectAsState()

                    DealsScreen(
                        deals = deals,
                        newAvailable = newAvailable,
                        isLoading = isDealsLoading,
                        isRefreshing = isRefreshing,
                        isInitialLoad = isInitialLoad,
                        hasMoreItems = hasMoreDealItems,
                        refreshCount = refreshCount,
                        onSearchButtonClick = { navController.navigate(route = NavigationItem.Search.route) },
                        loadMoreDeals = { dealsViewModel.loadMoreDeals() },
                        refreshDeals = { dealsViewModel.refreshDeals() }
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
                        loadMoreDeals = { searchViewModel.loadMoreDeals() },
                        onBackOperation = {
                            navController.popBackStack()
                        })
                }
                composable(NavigationItem.Notifications.route) {
                    NotificationScreen(
                        viewModel = notificationViewModel,
                        navController = navController
                    )
                }
                composable(NavigationItem.Profile.route) {
                    ProfileScreen(
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = {
                            settingsViewModel.toggleDarkMode()
                        },
                        contentScreenClick = { it ->
                            navController.navigate(NavigationItem.ContentScreen.withArgs(it))
                        },
                        appShare = {
                            appShare(context)
                        },
                        onNotificationSettingsClick = {
                            goToNotificationSettings(context)
                        },
                        onGenerateLinkClick = {
                            navController.navigate(NavigationItem.LinkGenerateScreen.route)
                        },
                        notificationEnabled = notificationEnabled,
                        onCheckNotificationStatus = {
                            settingsViewModel.checkNotificationStatus(context.applicationContext as Application)
                        }
                    )
                }

                composable(
                    route = NavigationItem.DealDetailScreen.route,
                    arguments = NavigationItem.DealDetailScreen.arguments
                ) { backStackEntry ->
                    val dealIdArg = backStackEntry.arguments?.getString("dealId")
                    DealDetailScreen(dealId = dealIdArg ?: "", context = context)
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

                composable(route = NavigationItem.LinkGenerateScreen.route){
                    //GenerateLink parameters
                    val generateLinkViewModel: GenerateLinkViewModel = viewModel()
                    val productLink by generateLinkViewModel.productLink.collectAsState()
                    val recentLinks by generateLinkViewModel.recentLinks.collectAsState()
                    val generatedLink by generateLinkViewModel.generatedLink.collectAsState()
                    val isLoading by generateLinkViewModel.isLoading.collectAsState()
                    val clipboardManager = LocalClipboardManager.current

                    GenerateLink(
                        productLink = productLink,
                        onProductLinkChange = {it -> generateLinkViewModel.onProductLinkChange(it)},
                        recentLinks = recentLinks,
                        listLinkClick = { it -> goToUrl(context,it)},
                        clipboardPaste = {
                            val clipText = clipboardManager.getText()?.text ?: ""
                            generateLinkViewModel.onProductLinkChange(clipText)
                        },
                        onGenerateLink = {
                            generateLinkViewModel.generateLink()
                        },
                        onDismissDialog = {generateLinkViewModel.clearGeneratedLink()},
                        isLoading = isLoading,
                        generatedLink = generatedLink
                    )
                }
            }
        }
    }
}

fun goToNotificationSettings(context: Context){
    val intent: Intent = Intent().apply {
        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}

fun appShare(context:Context){
    val shareText = "Check out OffnBuy for the best deals! Download it here:\n" +
            "https://play.google.com/store/apps/details?id=${context.packageName}"

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share OffnBuy via")
    context.startActivity(shareIntent)
}

fun goToUrl(context: Context, url: String){
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}