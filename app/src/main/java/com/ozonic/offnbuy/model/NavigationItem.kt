package com.ozonic.offnbuy.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

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

    object DealDetailScreen : NavigationItem(
        route = "dealDetail/{dealId}",
        arguments = listOf(navArgument("dealId") { type = NavType.StringType })
    ){
        fun withArgs(dealId: String): String {
            return route.replace("{dealId}", dealId)
        }
    }

    object ContentScreen : NavigationItem(
        route = "content/{contentType}",
        arguments = listOf(navArgument("contentType") { type = NavType.StringType })
    ){
        fun withArgs(contentType: String): String {
            return route.replace("{contentType}", contentType)
        }
    }

    object LinkGenerateScreen : NavigationItem(
        route = "linkGenerate"
    )

    companion object {
        fun values() = listOf(Home, Notifications, Profile)
    }
}

enum class Routes(val route: String){
    Home("Home"),
    Notifications("Notifications"),
    Profile("Profile"),
    Search("Search");
}

enum class ContentType(val route: String){
    TermsAndConditions("TermsAndConditions"),
    PrivacyPolicy("PrivacyPolicy"),
    HelpAndSupport("HelpAndSupport");
}