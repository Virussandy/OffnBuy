package com.ozonic.offnbuy.presentation.navigation

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ozonic.offnbuy.di.AppModule
import com.ozonic.offnbuy.di.ViewModelModule
import com.ozonic.offnbuy.domain.model.NavigationItem
import com.ozonic.offnbuy.domain.model.Routes
import com.ozonic.offnbuy.presentation.viewmodel.NotificationViewModel
import com.ozonic.offnbuy.presentation.viewmodel.NotificationViewModelFactory

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Correct instantiation using the factory with dependencies
    val context = LocalContext.current
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(
            getNotificationsUseCase = ViewModelModule.provideGetNotificationsUseCase(context),
            sharedPrefManager = AppModule.provideSharedPrefManager(context)
        )
    )
    val unseenCount by notificationViewModel.unseenCount.collectAsState()

    NavigationBar {
        NavigationItem.values().forEach {
            NavigationBarItem(
                icon = {
                    if (it.route == Routes.Notifications.route) {
                        BadgedBox(
                            badge = {
                                if (unseenCount > 0) {
                                    Badge {
                                        Text(text = unseenCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(it.icon!!, contentDescription = it.route)
                        }
                    } else {
                        Icon(it.icon!!, contentDescription = it.route)
                    }
                },
                onClick = {
                    if (currentRoute != it.route) {
                        navController.navigate(route = it.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                selected = currentRoute == it.route,
                label = { Text(text = it.route, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}