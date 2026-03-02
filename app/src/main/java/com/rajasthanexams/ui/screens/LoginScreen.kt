package com.rajasthanexams.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rajasthanexams.ui.components.AppButton
import com.rajasthanexams.ui.components.HeritagePatternBackground

import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rajasthanexams.ui.viewmodels.LoginViewModel
import com.rajasthanexams.ui.viewmodels.LoginUiState

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    // No more local state "phoneNumber" needed
    
    HeritagePatternBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            val uiState by viewModel.uiState.collectAsState()
            val mobile by viewModel.mobileNumber.collectAsState()
            val otp by viewModel.otp.collectAsState()

            LaunchedEffect(uiState) {
                if (uiState is LoginUiState.LoggedIn) {
                    onLoginSuccess()
                }
            }
            
            val context = androidx.compose.ui.platform.LocalContext.current
            LaunchedEffect(Unit) {
               viewModel.otpReceived.collect { otpMsg ->
                   android.widget.Toast.makeText(context, otpMsg, android.widget.Toast.LENGTH_LONG).show()
               }
            }

            Text(
                text = if (uiState is LoginUiState.OtpSent) "Enter OTP" else "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (uiState is LoginUiState.OtpSent) "OTP sent to +91 $mobile" else "Enter your mobile number to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState !is LoginUiState.OtpSent) {
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { if (it.length <= 10) viewModel.mobileNumber.value = it },
                    label = { Text("Mobile Number") },
                    prefix = { Text("+91 ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is LoginUiState.Loading
                )
            } else {
                OutlinedTextField(
                    value = otp,
                    onValueChange = { viewModel.otp.value = it },
                    label = { Text("Enter OTP") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is LoginUiState.Loading
                )
            }

            if (uiState is LoginUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                // Show Toast for visibility
                val context = androidx.compose.ui.platform.LocalContext.current
                LaunchedEffect(uiState) {
                     android.widget.Toast.makeText(context, (uiState as LoginUiState.Error).message, android.widget.Toast.LENGTH_LONG).show()
                }
            }
            
            if (uiState is LoginUiState.Loading) {
                 Spacer(modifier = Modifier.height(16.dp))
                 CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text = if (uiState is LoginUiState.OtpSent) "Verify & Login" else "Get OTP",
                onClick = { 
                    if (uiState is LoginUiState.OtpSent) viewModel.verifyOtp() else viewModel.sendOtp()
                },
                enabled = if (uiState is LoginUiState.OtpSent) otp.length >= 4 else mobile.length == 10
            )
        }
    }
}
