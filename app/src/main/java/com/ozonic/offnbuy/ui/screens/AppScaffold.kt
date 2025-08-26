package com.ozonic.offnbuy.presentation.ui.screens

import android.view.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ozonic.offnbuy.di.AppModule
import com.ozonic.offnbuy.presentation.navigation.BottomNavBar
import com.ozonic.offnbuy.presentation.ui.LocalSnackbarHostState
import com.ozonic.offnbuy.presentation.viewmodel.MainViewModel
import com.ozonic.offnbuy.presentation.viewmodel.MainViewModelFactory

/**
 * A universal scaffold for the app that includes the global snackbar host and offline banner.
 * It can also conditionally display the main bottom navigation bar.
 */
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    showBottomNav: Boolean = false,
    navController: NavController? = null,
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    content: @Composable (PaddingValues) -> Unit
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(AppModule.provideNetworkConnectivityObserver(navController!!.context))
    )
    val isOnline by mainViewModel.isOnline.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Column {
                // The OfflineBanner will always be checked and shown if needed.
                OfflineBanner(isOnline = isOnline)
                // The BottomNavBar is shown conditionally.
                if (showBottomNav && navController != null) {
                    BottomNavBar(navController = navController)
                }else{
                    Spacer(Modifier.navigationBarsPadding())
                }
            }
        },
        floatingActionButtonPosition = floatingActionButtonPosition,
        content = content
    )
}