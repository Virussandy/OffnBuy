package com.ozonic.offnbuy.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.PhoneAuthProvider
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.model.ContentType
import com.ozonic.offnbuy.model.NavigationItem
import com.ozonic.offnbuy.util.findActivity
import com.ozonic.offnbuy.viewmodel.AuthState
import com.ozonic.offnbuy.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onAuthComplete: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var previousState by remember { mutableStateOf<AuthState>(AuthState.Unauthenticated) }

    LaunchedEffect(authState) {
        val currentState = authState

        // Now, perform the check and use the local variable.
        // The compiler can now safely smart-cast 'currentState'.
        if (currentState is AuthState.Authenticated && !currentState.user.isAnonymous) {
            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
            onAuthComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val backgroundState = if (authState is AuthState.Loading) previousState else authState

        // Step 2: Draw the background UI.
        when (backgroundState) {
            // Treat anonymous users the same as unauthenticated users for this screen's UI.
            is AuthState.Authenticated -> {
                if (backgroundState.user.isAnonymous) {
                    LoginOptionsScreen(
                        navController = navController,
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = { phoneNumber = it },
                        error = null, // No error message on initial view
                        onSendOtp = { phone -> authViewModel.sendOtp(phone, context.findActivity()) }
                    )
                } else {
                    // If the user is NOT anonymous, they have just logged in.
                    // Show a blank screen while the LaunchedEffect navigates away.
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                }
            }

            is AuthState.Unauthenticated, is AuthState.AuthError -> {
                LoginOptionsScreen(
                    navController = navController,
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = { phoneNumber = it },
                    error = if (backgroundState is AuthState.AuthError) backgroundState.message else null,
                    onSendOtp = { phone -> authViewModel.sendOtp(phone, context.findActivity()) }
                )
            }
            is AuthState.AwaitingOtp -> {
                OtpScreen(
                    state = backgroundState,
                    onVerifyOtp = { verificationId, otp -> authViewModel.verifyOtp(verificationId, otp) },
                    onResendOtp = { phone, token -> authViewModel.sendOtp(phone, context.findActivity(), token) },
                    onChangeNumber = { authViewModel.cancelVerification() }
                )
            }
            else -> {
                // Handles the initial Loading state
//                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                LoadingOverlay(modifier = Modifier.fillMaxSize())
            }
        }

        // Step 3: If the current state is Loading, draw the transparent overlay on TOP of the UI from Step 2.
        if (authState is AuthState.Loading) {
            LoadingOverlay(modifier = Modifier.fillMaxSize())
        }
    }
}


@Composable
fun LoginOptionsScreen(
    navController: NavController,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    error: String?,
    onSendOtp: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.2f))

//        Icon(
//            painter = painterResource(id = R.drawable.icon),
//            contentDescription = "App Icon",
//            tint = MaterialTheme.colorScheme.primary,
//            modifier = Modifier.size(64.dp)
//        )
        Icon(
            painter = painterResource(id = R.drawable.icon),
            contentDescription = "App Icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Welcome to OffnBuy", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Enter your phone number to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { if (it.length <= 10) onPhoneNumberChange(it) },
            modifier = Modifier.fillMaxWidth(),
            prefix = {
                Text("+91 ", style = MaterialTheme.typography.bodyLarge)
            },
            label = { Text("10-digit mobile number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                if (phoneNumber.length == 10) onSendOtp("+91$phoneNumber")
            }),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                onSendOtp("+91$phoneNumber")
                      },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = phoneNumber.length == 10
        ) {
            Text("Continue", fontSize = 16.sp)
        }

        error?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.weight(1f))
        LegalTextFooter(navController = navController)
    }
}

@Composable
fun OtpScreen(
    state: AuthState.AwaitingOtp,
    onVerifyOtp: (String, String) -> Unit,
    onResendOtp: (String, PhoneAuthProvider.ForceResendingToken?) -> Unit,
    onChangeNumber: () -> Unit
) {
    var otpValue by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text("Verify Phone Number", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Enter the 6-digit code sent to ${state.phoneNumber.replace("+91", "")}",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onChangeNumber) {
            Text("Change number?")
        }
        Spacer(Modifier.height(32.dp))

        OtpInputField(
            otpText = otpValue,
            onOtpTextChange = { value, isComplete ->
                otpValue = value
                if (isComplete) {
                    focusManager.clearFocus()
                    onVerifyOtp(state.verificationId, value)
                }
            }
        )

        // Display error message directly on this screen
        state.error?.let {
            Spacer(Modifier.height(16.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onVerifyOtp(state.verificationId, otpValue) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = otpValue.length == 6 && !state.isVerifying
        ) {
            if (state.isVerifying) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Verify", fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(16.dp))

        ResendOtpButton(
            isResending = state.isResending,
            onResend = { onResendOtp(state.phoneNumber, state.resendToken) }
        )
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OtpInputField(
    modifier: Modifier = Modifier,
    otpText: String,
    otpCount: Int = 6,
    onOtpTextChange: (String, Boolean) -> Unit
) {
    val focusRequesters = remember { List(otpCount) { FocusRequester() } }

    LaunchedEffect(Unit) {
        focusRequesters.firstOrNull()?.requestFocus()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (i in 0 until otpCount) {
            OutlinedTextField(
                modifier = Modifier
                    .width(48.dp)
                    .focusRequester(focusRequesters[i])
                    .onKeyEvent { event ->
                        if (event.key == Key.Backspace && otpText.getOrNull(i) == null) {
                            if (i > 0) focusRequesters[i - 1].requestFocus()
                            return@onKeyEvent true
                        }
                        false
                    },
                value = otpText.getOrNull(i)?.toString() ?: "",
                onValueChange = { value ->
                    if (value.length <= 1) {
                        val newChar = value.getOrNull(0)?.toString() ?: ""
                        val paddedOtp = otpText.padEnd(otpCount, ' ')
                        val newOtp = paddedOtp.take(i) + newChar + paddedOtp.drop(i + 1)

                        val isComplete = newOtp.none { it == ' ' } // ✅ all filled

                        onOtpTextChange(newOtp.trimEnd(), isComplete)

                        if (newChar.isNotEmpty() && i < otpCount - 1) {
                            focusRequesters[i + 1].requestFocus()
                        }
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Next // ✅ force Next always
                ),
                keyboardActions = KeyboardActions(
                    onNext = { if (i < otpCount - 1) focusRequesters[i + 1].requestFocus() },
                    onDone = {
                        val paddedOtp = otpText.padEnd(otpCount, ' ')
                        val isComplete = paddedOtp.none { it == ' ' }
                        if (isComplete) {
                            onOtpTextChange(paddedOtp.trimEnd(), true)
                        }
                        // else: ignore, don’t auto-submit
                    }
                )
            )
        }
    }
}




@Composable
fun LegalTextFooter(navController: NavController) {
    val annotatedString = buildAnnotatedString {
        append("By continuing, you agree to our ")
        pushStringAnnotation(tag = "TOS", annotation = "TOS")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
            append("Terms of Service")
        }
        pop()
        append(" and ")
        pushStringAnnotation(tag = "PRIVACY", annotation = "PRIVACY")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
            append("Privacy Policy")
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "TOS", start = offset, end = offset).firstOrNull()?.let {
                navController.navigate(NavigationItem.ContentScreen.withArgs(ContentType.TermsAndConditions.route))
            }
            annotatedString.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset).firstOrNull()?.let {
                navController.navigate(NavigationItem.ContentScreen.withArgs(ContentType.PrivacyPolicy.route))
            }
        }
    )
}

@Composable
fun ResendOtpButton(
    isResending: Boolean,
    onResend: () -> Unit
) {
    var timer by remember { mutableStateOf(30) }
    // The timer is now only reset when the isResending state becomes true
    var isTimerRunning by remember(isResending) { mutableStateOf(true) }

    LaunchedEffect(key1 = isTimerRunning) {
        if (isTimerRunning) {
            for (i in 30 downTo 1) {
                timer = i
                delay(1000)
            }
            isTimerRunning = false
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Didn't receive code? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (isResending) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            TextButton(
                onClick = {
                    if (!isTimerRunning) {
                        onResend()
                    }
                },
                enabled = !isTimerRunning
            ) {
                Text(if (isTimerRunning) "Resend in ${timer}s" else "Resend Code")
            }
        }
    }
}