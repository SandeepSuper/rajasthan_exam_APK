package com.rajasthanexams.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rajasthanexams.ui.components.AppButton
import com.rajasthanexams.ui.components.AvatarHelper
import com.rajasthanexams.ui.components.AvatarPickerDialog
import com.rajasthanexams.ui.components.HeritagePatternBackground
import com.rajasthanexams.ui.theme.RoyalBlue
import com.rajasthanexams.ui.viewmodels.LoginViewModel

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
    var selectedAvatarId by remember { mutableStateOf<String?>(
        if (AvatarHelper.isAvatar(initialProfilePicture)) initialProfilePicture else "avatar_1"
    ) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var referredByCode by remember { mutableStateOf("") }

    val isEmailEditable = initialEmail.isNullOrBlank()

    HeritagePatternBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Back button
            if (onBackClick != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .clickable { onBackClick() }
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Avatar display with edit button
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.size(100.dp)
            ) {
                // Avatar circle
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEEF2FF),
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { showAvatarPicker = true },
                    shadowElevation = 4.dp
                ) {
                    val drawableRes = AvatarHelper.getDrawableRes(selectedAvatarId)
                    if (drawableRes != null) {
                        Image(
                            painter = painterResource(id = drawableRes),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = RoyalBlue,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
                // Edit badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { showAvatarPicker = true },
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Change Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tap to change avatar",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = isEmailEditable,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Gray,
                    disabledBorderColor = Color.LightGray,
                    disabledLabelColor = Color.Gray
                )
            )

            // Referral code field — only for new users (first-time profile setup)
            if (isEmailEditable) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = referredByCode,
                    onValueChange = { referredByCode = it.uppercase().trim() },
                    label = { Text("Referral Code (Optional)") },
                    placeholder = { Text("e.g. RAJ-XYZABC") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.Redeem,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(32.dp))

            AppButton(
                text = if (isLoading) "Saving..." else "Save & Continue",
                onClick = {
                    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
                    when {
                        name.isBlank() || email.isBlank() ->
                            errorMessage = "Please fill all fields"
                        !email.matches(emailPattern.toRegex()) ->
                            errorMessage = "Invalid email address"
                        else -> {
                            isLoading = true
                            errorMessage = null
                            viewModel.updateProfile(
                                name,
                                email,
                                selectedAvatarId,
                                referredByCode.ifBlank { null }
                            ) { success, error ->
                                isLoading = false
                                if (success) onContinueClick()
                                else errorMessage = error ?: "Failed to update profile. Try again."
                            }
                        }
                    }
                },
                enabled = !isLoading
            )
        }
    }

    // Avatar picker bottom sheet
    if (showAvatarPicker) {
        AvatarPickerDialog(
            selectedAvatarId = selectedAvatarId,
            onAvatarSelected = { avatarId -> selectedAvatarId = avatarId },
            onDismiss = { showAvatarPicker = false }
        )
    }
}
