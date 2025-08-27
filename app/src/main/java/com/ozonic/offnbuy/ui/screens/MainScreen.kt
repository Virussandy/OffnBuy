package com.ozonic.offnbuy.presentation.ui.screens

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ozonic.offnbuy.di.AppModule
import com.ozonic.offnbuy.presentation.navigation.AppNavigation
import com.ozonic.offnbuy.presentation.theme.OffnBuyTheme
import com.ozonic.offnbuy.presentation.viewmodel.AuthViewModel
import com.ozonic.offnbuy.presentation.viewmodel.AuthViewModelFactory
import com.ozonic.offnbuy.presentation.viewmodel.SettingsViewModel
import com.ozonic.offnbuy.presentation.viewmodel.SettingsViewModelFactory

@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel
) {
    val settingsState by settingsViewModel.settingsState.collectAsState()

    OffnBuyTheme(darkTheme = settingsState.isDarkMode, dynamicColor = settingsState.isDynamicColor) {
        AppNavigation(navHostController = navController, authViewModel = authViewModel, settingsViewModel = settingsViewModel)
    }
}