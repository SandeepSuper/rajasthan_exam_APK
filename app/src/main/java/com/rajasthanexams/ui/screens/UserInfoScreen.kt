package com.rajasthanexams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajasthanexams.ui.components.AppButton
import com.rajasthanexams.ui.components.HeritagePatternBackground
import com.rajasthanexams.ui.viewmodels.LoginViewModel
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import com.rajasthanexams.ui.theme.RoyalBlue
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    viewModel: LoginViewModel,
    onContinueClick: () -> Unit,
    initialName: String? = null,
    initialEmail: String? = null,
    initialProfilePicture: String? = null,
    onBackClick: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialName ?: "") }
    var email by remember { mutableStateOf(initialEmail ?: "") }
    var profilePictureUrl by remember { mutableStateOf<String?>(initialProfilePicture) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val isEmailEditable = initialEmail.isNullOrBlank()
    val uiState by viewModel.uiState.collectAsState()

    // Image Picker Launcher
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            isLoading = true
            viewModel.uploadProfilePicture(uri, context) { success, url ->
                isLoading = false
                if (success && url != null) {
                    profilePictureUrl = url
                } else {
                    errorMessage = "Failed to upload image"
                }
            }
        }
    }

    HeritagePatternBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (onBackClick != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                     Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .clickable { onBackClick() }
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Profile Picture Placeholder / Image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.LightGray, androidx.compose.foundation.shape.CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profilePictureUrl != null) {
                    coil.compose.AsyncImage(
                        model = profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize().padding(2.dp).background(Color.White, androidx.compose.foundation.shape.CircleShape).clip(androidx.compose.foundation.shape.CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Person,
                        contentDescription = "Add Photo",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                 if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = RoyalBlue)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tap to change photo", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isEmailEditable) "Complete Your Profile" else "Update Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Please enter your details to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("User Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                shape = RoundedCornerShape(12.dp),
                enabled = isEmailEditable,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Gray,
                    disabledBorderColor = Color.LightGray,
                    disabledLabelColor = Color.Gray
                )
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(32.dp))

            AppButton(
                text = if (isLoading) "Saving..." else "Save & Continue",
                onClick = {
                    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
                    if (name.isBlank() || email.isBlank()) {
                        errorMessage = "Please fill all fields"
                    } else if (!email.matches(emailPattern.toRegex())) {
                        errorMessage = "Invalid email address"
                    } else {
                        isLoading = true
                        errorMessage = null
                        // Pass profilePictureUrl
                        viewModel.updateProfile(name, email, profilePictureUrl) { success, error ->
                             isLoading = false
                             if (success) {
                                 onContinueClick()
                             } else {
                                 errorMessage = error ?: "Failed to update profile. Try again."
                             }
                        }
                    }
                },
                enabled = !isLoading
            )
        }
    }
}
