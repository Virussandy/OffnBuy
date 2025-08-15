package com.ozonic.offnbuy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.ImagesearchRoller
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.model.ContentType
import com.ozonic.offnbuy.viewmodel.AuthState
import com.ozonic.offnbuy.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    isDynamicColor: Boolean,
    onToggleDynamicColor: () -> Unit,
    contentScreenClick: (String) -> Unit,
    appShare: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    notificationEnabled: Boolean,
    onCheckNotificationStatus: () -> Unit,
    onLoginClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    onEditProfileClick: () -> Unit,
) {

    val authState by authViewModel.authState.collectAsState()
    val scrollState = rememberScrollState()
    val lifeCycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onCheckNotificationStatus()
            }
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(state = scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(1.dp))

            when (val state = authState) {
                is AuthState.Authenticated -> AuthenticatedHeader(state, onEditProfileClick)
                else -> UnauthenticatedHeader(onLoginClick)
            }

            // --- Settings Section ---
            Card(modifier = Modifier.fillMaxWidth()) {
                ProfileOption(
                    text = "Dark Mode",
                    icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    trailingContent = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onToggleDarkMode() }
                        )
                    }
                )
            }

            // --- Settings Section ---
            Card(modifier = Modifier.fillMaxWidth()) {
                ProfileOption(
                    text = "Dynamic Theme",
                    icon = if (isDynamicColor) Icons.Filled.FormatPaint else Icons.Filled.ImagesearchRoller,
                    trailingContent = {
                        Switch(
                            checked = isDynamicColor,
                            onCheckedChange = { onToggleDynamicColor() }
                        )
                    }
                )
            }

            // --- Account Section ---
            Card(modifier = Modifier.fillMaxWidth()) {
                ProfileOption(
                    text = "Edit Profile",
                    icon = Icons.Default.Edit,
                    onClick = onEditProfileClick
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NotificationProfileOption(
                    text = "Notification Settings",
                    icon = Icons.Default.Notifications,
                    onClick = onNotificationSettingsClick,
                    showBadge = !notificationEnabled
                )
            }


            // --- About & Support Section ---
            Card(modifier = Modifier.fillMaxWidth()) {
                ProfileOption(
                    text = "Share App",
                    icon = Icons.Default.Share,
                    onClick = appShare
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileOption(
                    text = "Help & Support",
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    onClick = { contentScreenClick(ContentType.HelpAndSupport.route) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileOption(
                    text = "Privacy Policy",
                    icon = Icons.Default.Policy,
                    onClick = { contentScreenClick(ContentType.PrivacyPolicy.route) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileOption(
                    text = "Terms & Conditions",
                    icon = Icons.Default.Assignment,
                    onClick = { contentScreenClick(ContentType.TermsAndConditions.route) }
                )
            }

            if (authState is AuthState.Authenticated) {
                Button(
                    onClick = { authViewModel.logout() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Logout")
                }
            }
        }
    }

@Composable
fun AuthenticatedHeader(state: AuthState.Authenticated, onEditProfileClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onEditProfileClick),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder for profile picture, using initial
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (state.user.displayName?.firstOrNull() ?: 'U').toString().uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hi, ${state.user.displayName ?: "User"}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = state.user.email ?: state.user.phoneNumber ?: "Welcome back",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UnauthenticatedHeader(onLoginClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Text(
                "Welcome to OffnBuy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Log in or sign up to save your preferences and more.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onLoginClick,
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Login / Sign Up")
            }
        }
    }
}

@Composable
private fun ProfileOption(
    text: String,
    icon: ImageVector,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = {
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
) {
    ListItem(
        modifier = Modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        headlineContent = { Text(text) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun NotificationProfileOption(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    showBadge: Boolean
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(text) },
        leadingContent = {
            BadgedBox(badge = {
                if (showBadge) {
                    Badge(containerColor = MaterialTheme.colorScheme.error)
                }
            }) {
                Icon(icon, contentDescription = null)
            }
        },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}