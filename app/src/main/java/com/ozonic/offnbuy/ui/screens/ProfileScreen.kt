package com.ozonic.offnbuy.presentation.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.domain.model.ContentType
import com.ozonic.offnbuy.domain.model.NavigationItem
import com.ozonic.offnbuy.domain.model.User
import com.ozonic.offnbuy.domain.model.UserProfile
import com.ozonic.offnbuy.presentation.viewmodel.AuthViewModel
import com.ozonic.offnbuy.presentation.viewmodel.SettingsViewModel
import com.ozonic.offnbuy.util.appShare

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val currentUser by authViewModel.currentUser.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val settingsState by settingsViewModel.settingsState.collectAsState()

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirmLogout = {
                showLogoutDialog = false
                authViewModel.signOut()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    AppScaffold(
        navController = navController,
        showBottomNav = true,
        topBar = { TopAppBar(title = { Text("Profile",style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(state = scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentUser?.isAnonymous == false) {
                AuthenticatedHeader(userProfile, onEditProfileClick = {
                    navController.navigate(NavigationItem.EditProfileScreen.route)
                })
            } else {
                UnauthenticatedHeader(onLoginClick = {
                    navController.navigate(NavigationItem.AuthScreen.route)
                })
            }

            SettingsSection(
                settingsState = settingsState,
                onToggleDarkMode = { settingsViewModel.toggleDarkMode() },
                onToggleDynamicColor = { settingsViewModel.toggleDynamicColor() },
                onNotificationSettingsClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                },
                onEditProfileClick = {navController.navigate(NavigationItem.EditProfileScreen.route)},
                isAnonymousUser = currentUser?.isAnonymous == true,
            )

            AboutAndSupportSection(
                onContentScreenClick = { route ->
                    navController.navigate(NavigationItem.ContentScreen.withArgs(route))
                },
                onAppShare = { appShare(context) }
            )

            if (currentUser?.isAnonymous == false) {
                LogoutButton(onLogoutClick = { showLogoutDialog = true })
            }
        }
    }
}

@Composable
fun AuthenticatedHeader(user: UserProfile?, onEditProfileClick: () -> Unit) {
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
            AsyncImage(
                model = user?.profilePic,
                contentDescription = "Profile Picture",
                placeholder = painterResource(id = R.drawable.ic_profile),
                error = painterResource(id = R.drawable.ic_profile),
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onEditProfileClick)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.name ?: "User",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user?.phone ?: user?.email ?: "Welcome back",
                    style = MaterialTheme.typography.bodyMedium,
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
            Image(
                painter = painterResource(R.drawable.icon),
                contentDescription = null,
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
                textAlign = TextAlign.Center
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
    settingsState: SettingsState,
    onToggleDarkMode: () -> Unit,
    onToggleDynamicColor: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onEditProfileClick: ()-> Unit,
    isAnonymousUser: Boolean,
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
        if(!isAnonymousUser) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ProfileOption(
                text = "Edit Profile",
                icon = Icons.Default.ManageAccounts,
                onClick = onEditProfileClick,
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
private fun LogoutButton(onLogoutClick: () -> Unit) {
    Button(
        onClick = onLogoutClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            "Logout",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun LogoutConfirmationDialog(onConfirmLogout: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Logout") },
        text = { Text("Are you sure you want to log out?") },
        confirmButton = {
            TextButton(
                onClick = onConfirmLogout,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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