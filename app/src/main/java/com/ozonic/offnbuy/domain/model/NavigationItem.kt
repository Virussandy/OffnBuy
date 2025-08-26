package com.ozonic.offnbuy.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * A sealed class representing all primary navigation destinations in the application.
 * This includes items for the bottom navigation bar as well as other screens.
 *
 * @param route The navigation route string.
 * @param icon The optional icon for the destination, used in the bottom navigation bar.
 * @param arguments A list of navigation arguments for destinations that require them.
 */
sealed class NavigationItem(
    val route: String,
    val icon: ImageVector? = null,
    val arguments: List<androidx.navigation.NamedNavArgument> = emptyList()
) {
    object Home : NavigationItem(
        route = Routes.Home.route,
        icon = Icons.Rounded.Home
    )

    object Notifications : NavigationItem(
        route = Routes.Notifications.route,
        icon = Icons.Rounded.Notifications
    )

    object Profile : NavigationItem(
        route = Routes.Profile.route,
        icon = Icons.Rounded.Person
    )

    object Search : NavigationItem(
        route = Routes.Search.route,
        icon = Icons.Rounded.Search
    )

    object Links : NavigationItem(
        route = Routes.Links.route,
        icon = Icons.Filled.Link
    )

    object DealDetailScreen : NavigationItem(
        route = "dealDetail/{dealId}",
        arguments = listOf(navArgument("dealId") { type = NavType.StringType })
    ) {
        fun withArgs(dealId: String): String {
            return route.replace("{dealId}", dealId)
        }
    }

    object ContentScreen : NavigationItem(
        route = "content/{contentType}",
        arguments = listOf(navArgument("contentType") { type = NavType.StringType })
    ) {
        fun withArgs(contentType: String): String {
            return route.replace("{contentType}", contentType)
        }
    }

    object AuthScreen : NavigationItem(
        route = "auth"
    )

    object EditProfileScreen : NavigationItem(
        route = "edit_profile"
    )

    companion object {
        fun values() = listOf(Home, Search, Links, Notifications, Profile)
    }
}