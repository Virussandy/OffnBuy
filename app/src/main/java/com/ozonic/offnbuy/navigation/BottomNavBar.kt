package com.ozonic.offnbuy.navigation

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ozonic.offnbuy.model.NavigationItem
import com.ozonic.offnbuy.model.Routes
import com.ozonic.offnbuy.viewmodel.NotificationViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BottomNavBar(navController: NavController, notificationViewModel: NotificationViewModel) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val notification = notificationViewModel.unseenCount.collectAsState()


    NavigationBar {
        NavigationItem.values().forEach {
            NavigationBarItem(
                icon = {
                    if(it.route == Routes.Notifications.route){
                        BadgedBox(
                            badge = {
                                if(notification.value > 0){
                                    Badge {
                                        Text(text = notification.value.toString())
                                    }
                                }
                            }
                        ){
                            Icon(it.icon!!, contentDescription = it.route)
                        }
                    }
                    else{
                        Icon(it.icon!!, contentDescription = it.route)
                    }

                },
                onClick = {
                    if(currentRoute != it.route){
                        navController.navigate(route = it.route){
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                selected = currentRoute == it.route,
                label = {Text(text = it.route, style = MaterialTheme.typography.labelSmall)},
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