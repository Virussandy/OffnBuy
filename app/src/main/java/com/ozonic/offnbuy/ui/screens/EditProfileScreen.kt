package com.ozonic.offnbuy.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.firebase.auth.PhoneAuthProvider
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.model.User
import com.ozonic.offnbuy.util.findActivity
import com.ozonic.offnbuy.viewmodel.AuthState
import com.ozonic.offnbuy.viewmodel.AuthViewModel
import com.ozonic.offnbuy.viewmodel.ProfileState

@Composable
fun EditProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val profileState by authViewModel.profileState.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showUpdatePhoneDialog by remember { mutableStateOf(false) }
    var showUpdateEmailDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { authViewModel.updateProfilePicture(it) }
    }

    LaunchedEffect(profileState) {
        if (profileState.isUpdateSuccessful) {
            val message = if (showUpdateEmailDialog) {
                "Verification email sent! Please check your inbox."
            } else {
                "Profile updated successfully!"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()

            // Close all dialogs on success
            showEditNameDialog = false
            showUpdatePhoneDialog = false
            showUpdateEmailDialog = false
            authViewModel.resetProfileState()
        }
        profileState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.resetProfileState()
        }
    }

    when (val state = authState) {
        is AuthState.Authenticated -> {
            EditProfileContent(
                user = state.user,
                profileState = profileState,
                onUpdateProfilePicture = { imagePickerLauncher.launch("image/*") },
                onEditName = { showEditNameDialog = true },
                onEditPhone = { showUpdatePhoneDialog = true },
                onEditEmail = { showUpdateEmailDialog = true }
            )
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator() // Show a loader while the user state is being resolved
            }
        }
    }

    // --- Dialogs ---
    if (showEditNameDialog && authState is AuthState.Authenticated) {
        EditNameDialog(
            profileState = profileState,
            currentName = (authState as AuthState.Authenticated).user.displayName ?: "",
            onDismiss = { showEditNameDialog = false },
            onSave = { newName -> authViewModel.updateDisplayName(newName) }
        )
    }

    if (showUpdatePhoneDialog) {
        UpdatePhoneDialog(
            profileState = profileState,
            onDismiss = { showUpdatePhoneDialog = false },
            onSendOtp = { phone -> authViewModel.sendOtpForUpdate(phone, context.findActivity()) },
            onVerifyOtp = { verificationId, otp ->
                val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                authViewModel.updateUserPhoneNumber(credential)
            }
        )
    }

    // This now correctly uses the UpdateEmailDialog for sending a verification link
    if (showUpdateEmailDialog) {
        UpdateEmailDialog(
            profileState = profileState,
            onDismiss = { showUpdateEmailDialog = false },
            onSendLink = { email -> authViewModel.sendVerificationEmail(email) }
        )
    }
}

@Composable
fun UpdateEmailDialog(
    profileState: ProfileState,
    onDismiss: () -> Unit,
    onSendLink: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Email Address") },
        text = {
            Column {
                Text("A verification link will be sent to your new email address. You must click the link to finalize the change.")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("New Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !profileState.isLoading,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSendLink(email) },
                enabled = !profileState.isLoading && email.isNotBlank()
            ) {
                if (profileState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Send Link")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

    @Composable
    fun EditProfileContent(
        user: User,
        profileState: ProfileState,
        onUpdateProfilePicture: () -> Unit,
        onEditName: () -> Unit,
        onEditPhone: () -> Unit,
        onEditEmail: () -> Unit,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProfilePicture(
                    photoUrl = user.photoUrl,
                    onUpdate = onUpdateProfilePicture
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Name") },
                            supportingContent = { Text(user.displayName ?: "Not set") },
                            trailingContent = {
                                TextButton(onClick = onEditName) { Text("Change") }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        ListItem(
                            headlineContent = { Text("Phone Number") },
                            supportingContent = { Text(user.phoneNumber ?: "Not set") },
                            trailingContent = {
                                TextButton(onClick = onEditPhone) { Text("Change") }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
//                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
//                        ListItem(
//                            headlineContent = { Text("Email Address") },
//                            supportingContent = {
//                                val emailText = user.email ?: "Not linked"
//                                val status = if (user.isEmailVerified) " (Verified)" else " (Not Verified)"
//                                Text(if (user.email != null) emailText + status else emailText)
//                            },
//                            trailingContent = {
//                                TextButton(onClick = onEditEmail) {
//                                    Text(if (user.email.isNullOrEmpty()) "Add" else "Change")
//                                }
//                            },
//                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
//                        ) // Correctly closed brace
                    }
                }
            }
        }
        if(profileState.isLoading) {
            LoadingOverlay(modifier = Modifier.fillMaxSize())
        }
    }

@Composable
fun ProfilePicture(photoUrl: Uri?, onUpdate: () -> Unit) {
    Box(contentAlignment = Alignment.BottomEnd) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Profile Picture",
            placeholder = painterResource(id = R.drawable.ic_profile),
            error = painterResource(id = R.drawable.ic_profile),
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable(onClick = onUpdate)
        )
        FloatingActionButton(
            onClick = onUpdate,
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Change profile picture")
        }
    }

}

@Composable
fun EditNameDialog(
    profileState: ProfileState,
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Your Name") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Name") },
                enabled = !profileState.isLoading,
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(newName) },
                enabled = !profileState.isLoading && newName.isNotBlank()
            ) {
                if (profileState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UpdatePhoneDialog(
    profileState: ProfileState,
    onDismiss: () -> Unit,
    onSendOtp: (String) -> Unit,
    onVerifyOtp: (String, String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    val isOtpView = profileState.phoneVerificationId != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (!isOtpView) "Update Phone Number" else "Enter OTP") },
        text = {
            Column {
                if (!isOtpView) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("New phone number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        enabled = !profileState.isLoading
                    )
                } else {
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it },
                        label = { Text("6-digit code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !profileState.isLoading
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isOtpView) {
                        onVerifyOtp(profileState.phoneVerificationId!!, otp)
                    } else {
                        onSendOtp("+91$phoneNumber")
                    }
                },
                enabled = !profileState.isLoading
            ) {
                if (profileState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(if (!isOtpView) "Send OTP" else "Verify & Update")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}