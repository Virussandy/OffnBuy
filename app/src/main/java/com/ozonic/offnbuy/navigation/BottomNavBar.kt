package com.ozonic.offnbuy.navigation

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
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


    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        NavigationItem.values().forEach {
            NavigationBarItem(
                icon = {
                    if(it.route == Routes.Notifications.route){
                        BadgedBox(
                            badge = {
                                if(notification.value > 0){
                                    Badge(containerColor = Color.Red, contentColor = Color.White) {
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
                label = {Text(it.route)}
            )
        }
    }
}
