package com.ozonic.offnbuy.ui.screens

import android.net.Uri
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.viewmodel.AuthState
import com.ozonic.offnbuy.viewmodel.AuthViewModel
import com.ozonic.offnbuy.viewmodel.EmailVerifyState
import com.ozonic.offnbuy.viewmodel.UpdatePhoneState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe the auth state to get the most recent user object
    val currentUser by remember(authState) {
        mutableStateOf(Firebase.auth.currentUser)
    }

    var showLinkPhoneDialog by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf(currentUser?.displayName ?: "User") }
    var userEmail by remember { mutableStateOf(currentUser?.email ?: "") }
    val emailVerifyState by authViewModel.emailVerifyState.collectAsState()
    var showEmailVerifyDialog by remember { mutableStateOf(false) }
    var emailToVerify by remember { mutableStateOf("") }

    // This state will hold the email a user is trying to verify
    var pendingEmail by remember { mutableStateOf("") }

    val isEmailVerified = currentUser?.isEmailVerified ?: false

    // Real-time validation for the email input
    val isEmailValid by remember(userEmail) {
        derivedStateOf {
            Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { authViewModel.updateProfilePicture(it) }
    }

    // Refresh user state when the app is resumed
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                authViewModel.reloadUser()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.AuthError -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            is AuthState.Authenticated -> {
                // Update the local state with fresh data from the ViewModel
                val freshUser = state.user
                userName = freshUser.displayName ?: "User"
                userEmail = freshUser.email ?: ""

                if (pendingEmail.isNotEmpty() && pendingEmail == freshUser.email && !freshUser.isEmailVerified) {
                    Toast.makeText(context, "Verification email sent to $pendingEmail", Toast.LENGTH_LONG).show()
                    pendingEmail = "" // Reset pending email
                }
            }
            else -> {}
        }
    }

    // This effect listens to the verification state to show/hide the dialog
    LaunchedEffect(emailVerifyState) {
        when (val state = emailVerifyState) {
            is EmailVerifyState.OtpSent -> showEmailVerifyDialog = true
            is EmailVerifyState.Verified -> {
                Toast.makeText(context, "Email successfully linked!", Toast.LENGTH_SHORT).show()
                showEmailVerifyDialog = false
                authViewModel.resetEmailVerifyState()
            }
            is EmailVerifyState.Error -> Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentUser != null) {
                // Profile picture UI (as provided in the previous response)

                AsyncImage(
                    model = currentUser?.photoUrl,
                    contentDescription = "Profile Picture",
                    placeholder = painterResource(id = R.drawable.ic_profile),
                    error = painterResource(id = R.drawable.ic_profile),
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") }
                )

                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { userEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = isEmailVerified,
                    isError = userEmail.isNotEmpty() && !isEmailValid,
                    trailingIcon = {
                        if (isEmailVerified) {
                            Text("Verified", color = MaterialTheme.colorScheme.primary)
                        } else if (userEmail.isNotEmpty() && isEmailValid) {
                            TextButton(
                                onClick = {
                                    emailToVerify = userEmail
                                    authViewModel.sendEmailOtp(userEmail)
                                },
                                enabled = emailVerifyState !is EmailVerifyState.Loading
                            ) {
                                Text("Verify")
                            }
                        }
                    }
                )


                Button(
                    onClick = {
                        // The save button now only saves the name
                        authViewModel.updateDisplayName(userName)
                        Toast.makeText(context, "Profile Saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authState !is AuthState.Loading
                ) {
                    // ... Loading indicator logic ...
                    Text("Save Name")
                }

                Divider()

                Text("Linked Accounts", style = MaterialTheme.typography.titleLarge)

                // --- Phone Number Row ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileInfoRow(
                        label = "Phone Number",
                        value = currentUser?.phoneNumber ?: "Not available"
                    )
                    TextButton(onClick = { showLinkPhoneDialog = true }) {
                        Text("Change")
                    }
                }

                if (isEmailVerified) {
                    ProfileInfoRow(label = "Email", value = currentUser?.email ?: "Not available")
                }
            } else {
                Text(
                    "You need to be logged in to edit your profile.",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        if (showLinkPhoneDialog) {
            UpdatePhoneDialog(
                authViewModel = authViewModel,
                onDismiss = {
                    authViewModel.resetLinkState()
                    showLinkPhoneDialog = false
                }
            )
        }

        // This will now correctly show the OTP dialog when needed
        if (showEmailVerifyDialog) {
            EmailVerifyDialog(
                email = emailToVerify,
                state = emailVerifyState,
                onDismiss = {
                    showEmailVerifyDialog = false
                    authViewModel.resetEmailVerifyState()
                },
                onVerify = { otp ->
                    authViewModel.verifyEmailAndLink(emailToVerify, otp)
                }
            )
        }
    }

/**
 * A dialog for entering the email verification OTP.
 */
@Composable
fun EmailVerifyDialog(
    email: String,
    state: EmailVerifyState,
    onDismiss: () -> Unit,
    onVerify: (String) -> Unit
) {
    var otp by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Verify Email") },
        text = {
            Column {
                Text("We've sent a verification code to $email. Please enter it below.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("Verification Code") },
                    enabled = state !is EmailVerifyState.Loading
                )
                // Instructions for the user during this simulation
                Text(
                    "For this demo, please use 'password123' as the code.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onVerify(otp) },
                enabled = state !is EmailVerifyState.Loading && otp.isNotEmpty()
            ) {
                if (state is EmailVerifyState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Verify & Link")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * A new dialog specifically for UPDATING the primary phone number.
 */
@Composable
fun UpdatePhoneDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val updateState by authViewModel.updatePhoneState.collectAsState()

    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdatePhoneState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            is UpdatePhoneState.Idle -> {
                if (otp.isNotEmpty()) { // If we just successfully updated
                    Toast.makeText(context, "Phone number updated!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            }
            else -> {}
        }
    }

    val isOtpView = updateState is UpdatePhoneState.AwaitingOtp

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
                        enabled = updateState !is UpdatePhoneState.Loading
                    )
                } else {
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it },
                        label = { Text("6-digit code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = updateState !is UpdatePhoneState.Loading
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isOtpView) {
                        val verificationId = (updateState as UpdatePhoneState.AwaitingOtp).verificationId
                        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                        authViewModel.updateUserPhoneNumber(credential)
                    } else {
                        authViewModel.sendOtpForUpdate(phoneNumber, context.findActivity())
                    }
                },
                enabled = updateState !is UpdatePhoneState.Loading
            ) {
                if (updateState is UpdatePhoneState.Loading) {
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

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}