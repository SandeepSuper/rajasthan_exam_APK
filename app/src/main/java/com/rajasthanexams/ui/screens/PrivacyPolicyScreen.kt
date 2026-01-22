package com.rajasthanexams.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rajasthanexams.ui.components.HeritagePatternBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit
) {
    HeritagePatternBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Last updated: January 15, 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SectionTitle("1. Introduction")
                SectionBody(
                    "Welcome to Rajasthan Exams Prep. We respect your privacy and are committed to protecting your personal data. " +
                    "This privacy policy will inform you as to how we look after your personal data when you visit our application and tell you about your privacy rights and how the law protects you."
                )

                SectionTitle("2. Data We Collect")
                SectionBody(
                    "We may collect, use, store and transfer different kinds of personal data about you which we have grouped together follows:\n" +
                    "• Identity Data: includes first name, last name, username or similar identifier.\n" +
                    "• Contact Data: includes email address and telephone numbers.\n" +
                    "• Technical Data: includes internet protocol (IP) address, your login data, browser type and version, time zone setting and location, browser plug-in types and versions, operating system and platform and other technology on the devices you use to access this website.\n" +
                    "• Usage Data: includes information about how you use our website, products and services."
                )

                SectionTitle("3. How We Use Your Data")
                SectionBody(
                    "We will only use your personal data when the law allows us to. Most commonly, we will use your personal data in the following circumstances:\n" +
                    "• Where we need to perform the contract we are about to enter into or have entered into with you.\n" +
                    "• Where it is necessary for our legitimate interests (or those of a third party) and your interests and fundamental rights do not override those interests.\n" +
                    "• Where we need to comply with a legal or regulatory obligation."
                )

                SectionTitle("4. Data Security")
                SectionBody(
                    "We have put in place appropriate security measures to prevent your personal data from being accidentally lost, used or accessed in an unauthorized way, altered or disclosed. " +
                    "In addition, we limit access to your personal data to those employees, agents, contractors and other third parties who have a business need to know."
                )

                SectionTitle("5. Contact Us")
                SectionBody(
                    "If you have any questions about this privacy policy or our privacy practices, please contact us at: support@rajasthanexams.com"
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun SectionBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    )
}
