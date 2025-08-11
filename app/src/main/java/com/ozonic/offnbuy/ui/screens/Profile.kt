package com.ozonic.offnbuy.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ozonic.offnbuy.model.ContentType
import java.nio.file.WatchEvent

@Composable
fun ProfileScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    contentScreenClick: (String) -> Unit,
    appShare: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onGenerateLinkClick: () -> Unit,
    notificationEnabled: Boolean,
    onCheckNotificationStatus: () -> Unit
) {

    val scrollState = rememberScrollState()
    val lifeCycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver{_, event ->
            if(event == Lifecycle.Event.ON_RESUME){
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header: Greeting and Avatar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hi, Valerie!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF888888))
                )
            }
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD8D8D8)),
                contentAlignment = Alignment.Center
            ) {
                Text("V", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Profile options
        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .clip(RoundedCornerShape(16.dp))
                .border(
                    border = BorderStroke(
                        width = 0.5.dp, color = MaterialTheme.colorScheme.primary
                    ), shape = MaterialTheme.shapes.small
                )
                //.background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp)
        ) {
            ProfileOption("Edit Profile", Icons.Default.Person)
            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            ProfileOption("Generate Links", Icons.Filled.Link, onClick = {
                onGenerateLinkClick()
            })
            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            NotificationProfileOption(
                "Notification Settings",
                Icons.Filled.Notifications,
                onClick = {
                    onNotificationSettingsClick()
                },
                showBadge = !notificationEnabled
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
//            .clip(RoundedCornerShape(16.dp))
                .border(
                    border = BorderStroke(
                        width = 0.5.dp, color = MaterialTheme.colorScheme.primary
                    ), shape = MaterialTheme.shapes.small
                )
                //.background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp)
        ) {
            DarkMode(isDarkMode = isDarkMode, onChanged = { onToggleDarkMode() })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
//            .clip(RoundedCornerShape(16.dp))
                .border(
                    border = BorderStroke(
                        width = 0.5.dp, color = MaterialTheme.colorScheme.primary
                    ), shape = MaterialTheme.shapes.small
                )
                //.background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp)
        ) {
            ProfileOption("Share App", Icons.Filled.Share, onClick = {
                appShare()
            })
            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            ProfileOption("Terms & Conditions", Icons.Filled.Assignment, onClick = {
                contentScreenClick(ContentType.TermsAndConditions.route)
            })
            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            ProfileOption("Privacy Policy", Icons.Filled.Security, onClick = {
                contentScreenClick(ContentType.PrivacyPolicy.route)
            })
            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            ProfileOption("Help & Support", Icons.Filled.SupportAgent, onClick = {
                contentScreenClick(ContentType.HelpAndSupport.route)
            })
        }
    }
}

@Composable
fun NotificationProfileOption(
    text: String,
    imageIcon: ImageVector,
    onClick: () -> Unit,
    showBadge: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        BadgedBox(badge = {
            if (showBadge) {
                Badge(containerColor = Color.Red)
            }
        }) {
            Icon(imageVector = imageIcon, contentDescription = null)
        }
    }
}

@Composable
private fun ProfileOption(
    text: String,
    imageIcon: ImageVector = Icons.Default.KeyboardArrowRight,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = imageIcon,
            contentDescription = null,
        )
    }
}

@Composable
private fun DarkMode(isDarkMode: Boolean = false, onChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Dark Mode",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.weight(1f)
        )
        Icon(imageVector = Icons.Filled.LightMode, contentDescription = null)
        Switch(
            checked = isDarkMode,
            onCheckedChange = onChanged
        )
        Icon(imageVector = Icons.Filled.DarkMode, contentDescription = null)
    }
}