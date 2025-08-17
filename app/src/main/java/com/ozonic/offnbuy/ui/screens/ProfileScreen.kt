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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.model.ContentType
import com.ozonic.offnbuy.viewmodel.AuthState
import com.ozonic.offnbuy.viewmodel.AuthViewModel

data class SettingsState(
    val isDarkMode: Boolean,
    val isDynamicColor: Boolean,
    val notificationEnabled: Boolean,
    val authState: AuthState
)

@Composable
fun ProfileScreen(
    settingsState: SettingsState,
    onToggleDarkMode: () -> Unit,
    onToggleDynamicColor: () -> Unit,
    onContentScreenClick: (String) -> Unit,
    onAppShare: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onLoginClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLogout: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(state = scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(1.dp))

        when (val state = settingsState.authState) {
            is AuthState.Authenticated -> AuthenticatedHeader(state, onEditProfileClick)
            else -> UnauthenticatedHeader(onLoginClick)
        }

        SettingsSection(
            settingsState = settingsState, // Pass the whole state object
            onToggleDarkMode = onToggleDarkMode,
            onToggleDynamicColor = onToggleDynamicColor,
            onEditProfileClick = onEditProfileClick,
            onNotificationSettingsClick = onNotificationSettingsClick
        )

        AboutAndSupportSection(onContentScreenClick, onAppShare)

        if (settingsState.authState is AuthState.Authenticated) {
            LogoutButton(onLogout)
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
private fun SettingsSection(
    settingsState: SettingsState, // Now receives the whole state
    onToggleDarkMode: () -> Unit,
    onToggleDynamicColor: () -> Unit,
    onEditProfileClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ProfileOption(
            text = "Dark Mode",
            icon = if (settingsState.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
            trailingContent = {
                Switch(
                    checked = settingsState.isDarkMode,
                    onCheckedChange = { onToggleDarkMode() }
                )
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        ProfileOption(
            text = "Dynamic Theme",
            icon = if (settingsState.isDynamicColor) Icons.Filled.FormatPaint else Icons.Filled.ImagesearchRoller,
            trailingContent = {
                Switch(
                    checked = settingsState.isDynamicColor,
                    onCheckedChange = { onToggleDynamicColor() }
                )
            }
        )
        if(settingsState.authState is AuthState.Authenticated){
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ProfileOption(
                text = "Edit Profile",
                icon = Icons.Default.Edit,
                onClick = onEditProfileClick
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        NotificationProfileOption(
            text = "Notification Settings",
            icon = Icons.Default.Notifications,
            onClick = onNotificationSettingsClick,
            showBadge = !settingsState.notificationEnabled
        )
    }
}

@Composable
private fun AboutAndSupportSection(
    onContentScreenClick: (String) -> Unit,
    onAppShare: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ProfileOption(
            text = "Share App",
            icon = Icons.Default.Share,
            onClick = onAppShare
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        ProfileOption(
            text = "Help & Support",
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            onClick = { onContentScreenClick(ContentType.HelpAndSupport.route) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        ProfileOption(
            text = "Privacy Policy",
            icon = Icons.Default.Policy,
            onClick = { onContentScreenClick(ContentType.PrivacyPolicy.route) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        ProfileOption(
            text = "Terms & Conditions",
            icon = Icons.AutoMirrored.Filled.Assignment,
            onClick = { onContentScreenClick(ContentType.TermsAndConditions.route) }
        )
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text("Logout", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 8.dp))
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