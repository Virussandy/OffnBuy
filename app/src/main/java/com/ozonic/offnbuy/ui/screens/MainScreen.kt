package com.ozonic.offnbuy.ui.screens

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.model.ContentType
import com.ozonic.offnbuy.model.NavigationItem
import com.ozonic.offnbuy.navigation.BottomNavBar
import com.ozonic.offnbuy.ui.screens.state.TopBarState
import com.ozonic.offnbuy.ui.theme.OffnBuyTheme
import com.ozonic.offnbuy.viewmodel.AuthViewModel
import com.ozonic.offnbuy.viewmodel.DealsViewModel
import com.ozonic.offnbuy.viewmodel.GenerateLinkViewModel
import com.ozonic.offnbuy.viewmodel.NotificationViewModel
import com.ozonic.offnbuy.viewmodel.SearchViewModel
import com.ozonic.offnbuy.viewmodel.SettingsViewModel
import java.util.regex.Pattern


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    dealIdState: MutableState<String?>,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()

    var topBarState by remember { mutableStateOf(TopBarState()) }
    var showBottomBar by remember { mutableStateOf(true) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    //Deals parameters
    val dealsViewModel: DealsViewModel = viewModel()
    val deals by dealsViewModel.deals.collectAsState()
    val newAvailable by dealsViewModel.newDealAvailable.collectAsState()

    //Notification parameters
    val notificationViewModel =
        remember { NotificationViewModel(context.applicationContext as Application) }
    val notificationCount by notificationViewModel.unseenCount.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    //Settings parameters
    val settingsViewModel =
        remember { SettingsViewModel(context.applicationContext as Application) }
    val isDarkMode by settingsViewModel.darkMode.collectAsState()
    val isDynamic by settingsViewModel.dynamicColor.collectAsState()
    val notificationEnabled by settingsViewModel.notificationsEnabled.collectAsState()

    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(navBackStackEntry) {
        showBottomBar = when(currentRoute){
            NavigationItem.ContentScreen.route -> false
            NavigationItem.Search.route -> false
            NavigationItem.DealDetailScreen.route -> false
            NavigationItem.AuthScreen.route -> false
            NavigationItem.EditProfileScreen.route -> false
            else -> true
        }

        topBarState = when(currentRoute){
            NavigationItem.Home.route -> TopBarState(
                title = {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Off",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "n",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.inversePrimary
                        )
                        Text(
                            "Buy",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 16.dp, bottom = 16.dp),
                        painter = painterResource(R.drawable.icon),
                        contentDescription = "Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(NavigationItem.Search.route) }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                floatingActionButton = {
                    if (newAvailable) {
                        ExtendedFloatingActionButton(
                            onClick = { dealsViewModel.refreshDeals() },
                            icon = { Icon(Icons.Default.Refresh, contentDescription = "refresh") },
                            text = { Text("Refresh for New Deals") }
                        )
                    }
                },
                floatingActionButtonPosition =  FabPosition.Center,
                scrollBehavior = scrollBehavior
            )

            NavigationItem.Links.route -> TopBarState(
                title = {Text(
                    "Links",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                ) },
            )

            NavigationItem.Notifications.route -> TopBarState(
                title = { Text(
                    "Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                ) },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mark All As Read") },
                                onClick = {
                                    expanded = false
                                    if (notificationCount > 0) {
                                        notificationViewModel.markAllAsSeen()
                                    }
                                }
                            )
                        }
                    }
                }
            )
            NavigationItem.Profile.route -> TopBarState(
                title = { Text(
                    "Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                ) },
            )
            NavigationItem.EditProfileScreen.route -> TopBarState(
                title = {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },

            )
            else -> TopBarState(isVisible = false)
        }
    }


    val dealId = dealIdState.value
    LaunchedEffect(dealId) {
        dealId?.let {
            notificationViewModel.markAsSeen(it)
            navController.navigate(NavigationItem.DealDetailScreen.withArgs(it))
            dealIdState.value = null
        }
    }

    OffnBuyTheme(
        darkTheme = isDarkMode,
        dynamicColor = isDynamic
    ) {
        Scaffold(
            modifier = Modifier,
            topBar = {
              if(topBarState.isVisible){
                  TopAppBar(
                      title = { topBarState.title?.invoke() },
                      navigationIcon = { topBarState.navigationIcon?.invoke() },
                      actions = { topBarState.actions?.invoke() },
                      windowInsets = WindowInsets.displayCutout,
                      scrollBehavior = topBarState.scrollBehavior
                  )
              }
            },
            floatingActionButton = {
                topBarState.floatingActionButton?.invoke()
            },
            floatingActionButtonPosition = FabPosition.Center,
            bottomBar = {
                if(showBottomBar){
                    BottomNavBar(
                        navController = navController,
                        notificationViewModel = notificationViewModel
                    )
                }
            },
           // contentWindowInsets = ScaffoldDefaults.contentWindowInsets
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = NavigationItem.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(NavigationItem.Home.route) {
                    //Deals parameters
                    val isInitialLoad by dealsViewModel.isInitialLoad.collectAsState()

                    val isDealsLoading by dealsViewModel.isLoading.collectAsState()
                    val isRefreshing by dealsViewModel.isRefreshing.collectAsState()
                    val hasMoreDealItems by dealsViewModel.hasMoreItems.collectAsState()
                    val refreshCount by dealsViewModel.refreshCount.collectAsState()

                    DealsScreen(
                        deals = deals,
                        isLoading = isDealsLoading,
                        isRefreshing = isRefreshing,
                        isInitialLoad = isInitialLoad,
                        hasMoreItems = hasMoreDealItems,
                        refreshCount = refreshCount,
                        loadMoreDeals = { dealsViewModel.loadMoreDeals() },
                        refreshDeals = { dealsViewModel.refreshDeals() },
                        scrollBehavior = scrollBehavior
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
                        isDynamicColor = isDynamic,
                        onToggleDynamicColor = {
                            settingsViewModel.toggleDynamicColor()
                        },
                        notificationEnabled = notificationEnabled,
                        onCheckNotificationStatus = {
                            settingsViewModel.checkNotificationStatus(context.applicationContext as Application)
                        },
                        authViewModel = authViewModel,
                        onLoginClick = {
                            navController.navigate("auth")
                        },
                        onEditProfileClick = {
                            navController.navigate("edit_profile")
                        }

                    )
                }

                composable(route = NavigationItem.AuthScreen.route) {
                    AuthScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        onAuthComplete = {
                            navController.popBackStack()
                        })
                }

                composable(route = NavigationItem.EditProfileScreen.route) {
                    val authViewModel : AuthViewModel = viewModel()
                    EditProfileScreen(authViewModel)
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

                composable(route = NavigationItem.Links.route){
                    //GenerateLink parameters
                    val generateLinkViewModel: GenerateLinkViewModel = viewModel()
                    val productLink by generateLinkViewModel.productLink.collectAsState()
                    val recentLinks by generateLinkViewModel.recentLinks.collectAsState()
                    val generatedLink by generateLinkViewModel.generatedLink.collectAsState()
                    val isLoading by generateLinkViewModel.isLoading.collectAsState()
                    val isError by generateLinkViewModel.isError.collectAsState()
                    val clipboardManager = LocalClipboardManager.current

                    GenerateLink(
                        productLink = productLink,
                        onProductLinkChange = {it -> generateLinkViewModel.onProductLinkChange(it)},
                        recentLinks = recentLinks,
                        listLinkClick = { it -> goToUrl(context,it)},
                        clipboardPaste = {
                            val clipText = clipboardManager.getText()?.text ?: ""
                            val url = extractUrl(clipText)
                            generateLinkViewModel.onProductLinkChange(url ?: clipText)
                        },
                        onGenerateLink = {
                            generateLinkViewModel.generateLink()
                        },
                        onDismissDialog = {generateLinkViewModel.clearGeneratedLink()},
                        isLoading = isLoading,
                        generatedLink = generatedLink,
                        isError = isError,
                    )
                }
            }
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