package com.ozonic.offnbuy.ui.screens

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.model.NavigationItem
import com.ozonic.offnbuy.navigation.AppNavigation
import com.ozonic.offnbuy.navigation.BottomNavBar
import com.ozonic.offnbuy.ui.screens.state.TopBarState
import com.ozonic.offnbuy.ui.theme.OffnBuyTheme
import com.ozonic.offnbuy.viewmodel.AuthState
import com.ozonic.offnbuy.viewmodel.AuthViewModel
import com.ozonic.offnbuy.viewmodel.DealsViewModel
import com.ozonic.offnbuy.viewmodel.NotificationViewModel
import com.ozonic.offnbuy.viewmodel.SettingsViewModel
import com.ozonic.offnbuy.viewmodel.SettingsViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(dealIdState: MutableState<String?>, authViewModel: AuthViewModel) {

    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // ViewModel Instantiations
    val dealsViewModel: DealsViewModel = viewModel()
    val notificationViewModel = remember { NotificationViewModel(context.applicationContext as Application) }
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(application, authViewModel)
    )
    val authState by authViewModel.authState.collectAsState()

    // State observers
    val refreshCount by dealsViewModel.refreshCount.collectAsState()
    val notificationCount by notificationViewModel.unseenCount.collectAsState()
    val newAvailable by dealsViewModel.newDealAvailable.collectAsState()
    val settingsState by settingsViewModel.settingsState.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var topBarState by remember { mutableStateOf(TopBarState()) }
    var showBottomBar by remember { mutableStateOf(true) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val currentRoute = navBackStackEntry?.destination?.route

    val gridState = rememberLazyGridState()

    val isFabVisible = remember { mutableStateOf(true) }

    // This connection directly listens to scroll gestures
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // User is scrolling down
                if (available.y < 0) {
                    isFabVisible.value = false
                }
                // User is scrolling up
                else if (available.y > 0) {
                    isFabVisible.value = true
                }
                return Offset.Zero
            }
        }
    }

    if (authState is AuthState.ReAuthenticationRequired) {
        ReAuthDialog(
            message = (authState as AuthState.ReAuthenticationRequired).message,
            navController = navController,
            onDismiss = { authViewModel.cancelVerification() }
        )
    }

    val isFabExpanded by remember {
        derivedStateOf {
            // Always expanded at the top, otherwise based on the scroll direction
            gridState.firstVisibleItemIndex == 0 || isFabVisible.value
        }
    }

    LaunchedEffect(refreshCount) {
        if (refreshCount > 0) { // Avoid scrolling on initial composition
            gridState.animateScrollToItem(0)
        }
    }


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
                            expanded = isFabExpanded,
                            onClick = { dealsViewModel.refreshDeals() },
                            icon = { Icon(Icons.Default.Refresh, contentDescription = "refresh") },
                            text = { Text("Refresh Deals")}
                        )
                    }
                },
                isFabExpanded = isFabExpanded,
                floatingActionButtonPosition = FabPosition.End,
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
        darkTheme = settingsState.isDarkMode,
        dynamicColor = settingsState.isDynamicColor
    ) {
        Scaffold(
            modifier = Modifier
                .nestedScroll(nestedScrollConnection)
                .then(
                    if (topBarState.scrollBehavior != null) {
                        Modifier.nestedScroll(topBarState.scrollBehavior!!.nestedScrollConnection)
                    } else {
                        Modifier
                    }
                ),
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
            floatingActionButtonPosition = topBarState.floatingActionButtonPosition,
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
            AppNavigation(
                navHostController = navController,
                modifier = Modifier.padding(paddingValues),
                dealsViewModel = dealsViewModel,
                notificationViewModel = notificationViewModel,
                authViewModel = authViewModel,
                settingsViewModel = settingsViewModel,
                gridState = gridState,
            )
        }
    }
}

@Composable
fun ReAuthDialog(message: String?, navController: NavController, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = {
            // We must handle a state change here
            onDismiss()
            navController.navigate(NavigationItem.AuthScreen.route) {
                popUpTo(0) // Clear the entire back stack
            }
        },
        title = { Text("Session Expired") },
        text = {
            if (message != null) {
                Text(message)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    navController.popBackStack()
                }
            ) {
                Text("OK")
            }
        }
    )
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