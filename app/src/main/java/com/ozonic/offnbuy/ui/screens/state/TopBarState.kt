package com.ozonic.offnbuy.ui.screens.state

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable

data class TopBarState @OptIn(ExperimentalMaterial3Api::class) constructor(
    val title: @Composable (() -> Unit)? = null,
    val navigationIcon: @Composable (() -> Unit)? = null,
    val actions: @Composable (() -> Unit)? = null,
    val isVisible: Boolean = true,
    val scrollBehavior: TopAppBarScrollBehavior? = null,
    val isFabExpanded: Boolean = true,
    val floatingActionButton: @Composable (() -> Unit)? = null,
    val floatingActionButtonPosition: FabPosition = FabPosition.Center,
    )