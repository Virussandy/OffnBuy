package com.ozonic.offnbuy.presentation.ui.screens

/**
 * Represents the UI state for the settings-related composables.
 *
 * @param isDarkMode A boolean indicating if dark mode is enabled.
 * @param isDynamicColor A boolean indicating if dynamic theming is enabled.
 * @param notificationEnabled A boolean indicating if notifications are enabled for the app.
 */
data class SettingsState(
    val isDarkMode: Boolean,
    val isDynamicColor: Boolean,
    val notificationEnabled: Boolean
)