package com.ozonic.offnbuy.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ozonic.offnbuy.presentation.viewmodel.AuthUiState
import com.ozonic.offnbuy.presentation.viewmodel.AuthViewModel
import com.ozonic.offnbuy.util.findActivity


@Composable
fun AuthScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onAuthComplete: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // This LaunchedEffect will listen for the final success state and navigate away.
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.AuthSuccess) {
            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
            onAuthComplete()
        }
    }

    AppScaffold(navController = navController, showBottomNav = false) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // The content of the screen is determined by the current UI state.
            when (val state = uiState) {
                is AuthUiState.Loading -> CircularProgressIndicator()
                is AuthUiState.EnterPhoneNumber -> PhoneNumberScreen(
                    error = state.error,
                    onSendOtp = { phone -> authViewModel.onPhoneNumberEntered(phone, context.findActivity()) }
                )
                is AuthUiState.EnterOtp -> OtpScreen(
                    phoneNumber = state.phoneNumber,
                    error = state.error,
                    onVerifyOtp = { otp -> authViewModel.onOtpEntered(otp) }
                )
                else -> {
                    // Handles Idle and AuthSuccess states, where no UI is needed in this Box.
                }
            }
        }
    }
}


@Composable
private fun PhoneNumberScreen(error: String?, onSendOtp: (String) -> Unit) {
    var phone by remember { mutableStateOf("") }
    val isButtonEnabled = phone.length == 10

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter your phone number", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "We'll send you a code to verify your number.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 10) phone = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("10-digit mobile number") },
            prefix = { Text("+91 ") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = if (isButtonEnabled) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onDone = { if (isButtonEnabled) onSendOtp("+91$phone") }),
            isError = error != null,
            singleLine = true
        )

        if (error != null) {
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onSendOtp("+91$phone") },
            enabled = isButtonEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send OTP")
        }
    }
}


@Composable
private fun OtpScreen(phoneNumber: String, error: String?, onVerifyOtp: (String) -> Unit) {
    var otp by remember { mutableStateOf("") }
    val isButtonEnabled = otp.length == 6

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Verify OTP", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Enter the 6-digit code sent to $phoneNumber",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) otp = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("6-Digit Code") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = if (isButtonEnabled) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onDone = { if (isButtonEnabled) onVerifyOtp(otp) }),
            isError = error != null,
            singleLine = true
        )

        if (error != null) {
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onVerifyOtp(otp) },
            enabled = isButtonEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify & Continue")
        }
    }
}