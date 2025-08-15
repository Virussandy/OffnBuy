package com.ozonic.offnbuy.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.model.ContentType
import com.ozonic.offnbuy.model.NavigationItem
import com.ozonic.offnbuy.util.findActivity
import com.ozonic.offnbuy.viewmodel.AuthState
import com.ozonic.offnbuy.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    onAuthComplete: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
            onAuthComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = authState) {
            is AuthState.Unauthenticated, is AuthState.AuthError -> {
                LoginOptionsScreen(
                    navController = navController,
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = { phoneNumber = it },
                    error = if (state is AuthState.AuthError) state.message else null,
                    onSendOtp = { phone ->
                        authViewModel.sendOtp(phone, context.findActivity())
                    }
                )
            }
            is AuthState.AwaitingOtp -> {
                BackHandler { authViewModel.goBackToLogin() }
                OtpScreen(
                    verificationId = state.verificationId,
                    phoneNumber = state.phoneNumber,
                    onVerifyOtp = { verificationId, otp ->
                        authViewModel.verifyOtp(verificationId, otp)
                    },
                    onResendOtp = { phone ->
                        authViewModel.sendOtp(phone, context.findActivity())
                    },
                    onGoBack = { authViewModel.goBackToLogin() }
                )
            }
            is AuthState.Loading -> {
                // Determine which screen to show underneath the loading overlay
                val previousState = authViewModel.previousAuthState
                if (previousState is AuthState.AwaitingOtp) {
                    OtpScreen(
                        verificationId = previousState.verificationId,
                        phoneNumber = previousState.phoneNumber,
                        onVerifyOtp = { _, _ -> },
                        onResendOtp = { },
                        onGoBack = { }
                    )
                } else {
                    LoginOptionsScreen(
                        navController = navController,
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = { phoneNumber = it },
                        error = null,
                        onSendOtp = {}
                    )
                }
                LoadingOverlay()
            }
            is AuthState.Authenticated -> {
                // Blank box to avoid flicker while navigating away
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            }
        }
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.2f))

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
                if (phoneNumber.length == 10) onSendOtp("+91$phoneNumber")
            }),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onSendOtp("+91$phoneNumber") },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    verificationId: String,
    phoneNumber: String,
    onVerifyOtp: (String, String) -> Unit,
    onResendOtp: (String) -> Unit,
    onGoBack: () -> Unit
) {
    var otpValue by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Verify Phone Number") },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter the 6-digit code sent to ${phoneNumber.replace("+91", "")}",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(40.dp))

            OtpInputField(
                otpText = otpValue,
                onOtpTextChange = { value, isComplete ->
                    otpValue = value
                    if (isComplete) {
                        focusManager.clearFocus()
                        onVerifyOtp(verificationId, value)
                    }
                }
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onVerifyOtp(verificationId, otpValue) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = otpValue.length == 6
            ) {
                Text("Verify", fontSize = 16.sp)
            }
            Spacer(Modifier.height(16.dp))

            ResendOtpButton(
                phoneNumber = phoneNumber,
                onResend = { onResendOtp(phoneNumber) }
            )
        }
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0 until otpCount) {
            OutlinedTextField(
                modifier = Modifier
                    .width(48.dp)
                    .focusRequester(focusRequesters[i])
                    .onKeyEvent { event ->
                        if (event.key == Key.Backspace && otpText.getOrNull(i) == null) {
                            if (i > 0) {
                                focusRequesters[i - 1].requestFocus()
                            }
                            return@onKeyEvent true
                        }
                        false
                    },
                value = otpText.getOrNull(i)?.toString() ?: "",
                onValueChange = { value ->
                    if (value.length <= 1) {
                        val newChar = value.getOrNull(0)?.toString() ?: ""
                        val newOtp = otpText.take(i) + newChar + otpText.drop(i + 1)
                        val isComplete = newOtp.length == otpCount
                        onOtpTextChange(newOtp, isComplete)
                        if (newChar.isNotEmpty() && i < otpCount - 1) {
                            focusRequesters[i + 1].requestFocus()
                        }
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = if (i == otpCount - 1) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { if (i < otpCount - 1) focusRequesters[i + 1].requestFocus() },
                    onDone = { /* Handled by onOtpTextChange */ }
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
    phoneNumber: String,
    onResend: (String) -> Unit
) {
    var timer by remember { mutableStateOf(30) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = isTimerRunning, key2 = onResend) {
        if (isTimerRunning) {
            launch {
                for (i in 30 downTo 1) {
                    timer = i
                    delay(1000)
                }
                isTimerRunning = false
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Didn't receive code? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(
            onClick = {
                if (!isTimerRunning) {
                    onResend(phoneNumber)
                    isTimerRunning = true
                }
            },
            enabled = !isTimerRunning
        ) {
            Text(if (isTimerRunning) "Resend in ${timer}s" else "Resend Code", fontWeight = FontWeight.Bold)
        }
    }
}