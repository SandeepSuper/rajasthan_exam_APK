package com.rajasthanexams.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajasthanexams.ui.components.AppButton
import com.rajasthanexams.ui.components.HeritagePatternBackground
import com.rajasthanexams.ui.viewmodels.LoginUiState
import com.rajasthanexams.ui.viewmodels.LoginViewModel
import kotlinx.coroutines.delay

@Composable
fun EmailOtpScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onBackToSignup: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val otp by viewModel.otp.collectAsState()
    val pendingEmail by viewModel.pendingEmail.collectAsState()

    // Resend OTP cooldown timer (30 seconds)
    var resendCooldown by remember { mutableStateOf(30) }
    var canResend by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (resendCooldown > 0) {
            delay(1000)
            resendCooldown--
        }
        canResend = true
    }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.LoggedIn) onLoginSuccess()
    }

    HeritagePatternBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = {
                    viewModel.resetState()
                    onBackToSignup()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Email icon
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Verify Your Email",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We sent a 6-digit OTP to",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = pendingEmail,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // OTP Input Field
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) viewModel.otp.value = it },
                label = { Text("Enter 6-digit OTP") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is LoginUiState.Loading,
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    letterSpacing = 8.sp,
                    fontSize = 22.sp
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Error
            AnimatedVisibility(
                visible = uiState is LoginUiState.Error,
                enter = fadeIn(), exit = fadeOut()
            ) {
                if (uiState is LoginUiState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (uiState as LoginUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (uiState is LoginUiState.Loading) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(28.dp))

            AppButton(
                text = "Verify & Continue",
                onClick = { viewModel.verifyEmailOtp() },
                enabled = otp.length == 6 && uiState !is LoginUiState.Loading
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Resend OTP
            if (canResend) {
                TextButton(onClick = {
                    canResend = false
                    resendCooldown = 30
                    viewModel.resendEmailOtp(pendingEmail)
                    viewModel.otp.value = ""
                }) {
                    Text("Resend OTP", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
                // Restart cooldown after resend
                LaunchedEffect(canResend) {
                    if (!canResend) {
                        while (resendCooldown > 0) {
                            delay(1000)
                            resendCooldown--
                        }
                        canResend = true
                    }
                }
            } else {
                Text(
                    text = "Resend OTP in ${resendCooldown}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
