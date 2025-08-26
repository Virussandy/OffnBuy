package com.ozonic.offnbuy.presentation.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.google.firebase.auth.PhoneAuthProvider
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.domain.model.User
import com.ozonic.offnbuy.domain.model.UserProfile
import com.ozonic.offnbuy.presentation.viewmodel.AuthViewModel
import com.ozonic.offnbuy.presentation.viewmodel.ProfileState
import com.ozonic.offnbuy.util.findActivity

private enum class EditDialog {
    NONE, NAME, EMAIL, PHONE, OTP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val currentUser by authViewModel.userProfile.collectAsState()
    val profileState by authViewModel.profileState.collectAsState()

    var currentDialog by remember { mutableStateOf(EditDialog.NONE) }
    var phoneNumberToVerify by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { authViewModel.updateProfilePicture(it) }
    }


    LaunchedEffect(profileState) {
        if (profileState.isUpdateSuccessful) {
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            currentDialog = EditDialog.NONE
            authViewModel.resetProfileState()
        }
        profileState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.resetProfileState()
        }
        profileState.phoneVerificationId?.let {
            currentDialog = EditDialog.OTP
        }
    }

    AppScaffold(
        navController = navController,
        showBottomNav = false,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            currentUser?.let { user ->
                EditProfileContent(
                    user = user,
                    onUpdateProfilePicture = { imagePickerLauncher.launch("image/*") },
                    onEditName = { currentDialog = EditDialog.NAME },
                    onEditEmail = { currentDialog = EditDialog.EMAIL },
                    onEditPhone = { currentDialog = EditDialog.PHONE }
                )

                val isDialogLoading = profileState.isLoading
                when (currentDialog) {
                    EditDialog.NAME -> EditDialog(
                        title = "Edit Name",
                        initialValue = user.name ?: "",
                        isLoading = isDialogLoading,
                        onDismiss = { currentDialog = EditDialog.NONE },
                        onSave = { newName -> authViewModel.updateDisplayName(newName) }
                    )

                    EditDialog.EMAIL -> EditDialog(
                        title = "Edit Email",
                        initialValue = user.email ?: "",
                        isLoading = isDialogLoading,
                        onDismiss = { currentDialog = EditDialog.NONE },
                        onSave = { newEmail -> authViewModel.verifyBeforeUpdateEmail(newEmail) },
                        keyboardType = KeyboardType.Email
                    )

                    EditDialog.PHONE -> EditDialog(
                        title = "Edit Phone",
                        initialValue = user.phone?.removePrefix("+91") ?: "",
                        isLoading = isDialogLoading,
                        onDismiss = { currentDialog = EditDialog.NONE },
                        onSave = { newPhone ->
                            phoneNumberToVerify = "+91$newPhone"
                            authViewModel.sendOtpForUpdate(
                                phoneNumberToVerify,
                                context.findActivity()
                            )
                        },
                        keyboardType = KeyboardType.Phone
                    )

                    EditDialog.OTP -> profileState.phoneVerificationId?.let { verificationId ->
                        OtpVerificationDialog(
                            verificationId = verificationId,
                            isLoading = isDialogLoading,
                            onDismiss = { currentDialog = EditDialog.NONE },
                            onVerify = { otp ->
                                val credential =
                                    PhoneAuthProvider.getCredential(verificationId, otp)
                                authViewModel.updatePhoneNumber(credential)
                            }
                        )
                    }

                    EditDialog.NONE -> {}
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            if (profileState.isLoading && currentDialog == EditDialog.NONE) {
                LoadingOverlay(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun EditDialog(
    title: String,
    initialValue: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { if (!isLoading) onSave(text) })
            )
        },
        confirmButton = {
            Button(onClick = { onSave(text) }, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OtpVerificationDialog(
    verificationId: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onVerify: (String) -> Unit
) {
    var otp by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Enter OTP") },
        text = {
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                label = { Text("6-Digit Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
            )
        },
        confirmButton = {
            Button(onClick = { onVerify(otp) }, enabled = otp.length == 6 && !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Verify")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditProfileContent(
    user: UserProfile,
    onUpdateProfilePicture: () -> Unit,
    onEditName: () -> Unit,
    onEditEmail: () -> Unit,
    onEditPhone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfilePicture(photoUrl = user.profilePic, onUpdate = onUpdateProfilePicture)
        Spacer(modifier = Modifier.height(16.dp))
        ProfileInfoItem(
            icon = Icons.Default.Person,
            title = "Name",
            value = user.name,
            onClick = onEditName
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        ProfileInfoItem(
            icon = Icons.Default.Email,
            title = "Email",
            value = user.email,
            onClick = onEditEmail
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        ProfileInfoItem(
            icon = Icons.Default.Phone,
            title = "Phone",
            value = user.phone,
            onClick = onEditPhone
        )
    }
}

@Composable
fun ProfilePicture(photoUrl: String?, onUpdate: () -> Unit) {
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
            Icon(Icons.Default.CameraAlt, "Change profile picture")
        }
    }
}

@Composable
fun ProfileInfoItem(icon: ImageVector, title: String, value: String?, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = { Text(value ?: "Not set", fontWeight = FontWeight.SemiBold) },
        leadingContent = {
            Icon(
                icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                Icons.Default.Edit,
                "Edit $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
fun EditDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onSave(text) })
            )
        },
        confirmButton = {
            Button(onClick = { onSave(text) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}