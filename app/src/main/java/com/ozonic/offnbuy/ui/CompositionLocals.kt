package com.ozonic.offnbuy.presentation.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

// Provides the global SnackbarHostState to any composable in the hierarchy.
val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}