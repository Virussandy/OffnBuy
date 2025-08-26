package com.ozonic.offnbuy.domain.model

/**
 * Defines the constant route strings for the main navigation destinations.
 * This helps avoid magic strings and ensures consistency.
 */
enum class Routes(val route: String) {
    Home("Home"),
    Notifications("Notifications"),
    Profile("Profile"),
    Search("Search"),
    Links("Links")
}