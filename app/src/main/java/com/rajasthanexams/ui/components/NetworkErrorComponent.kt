package com.rajasthanexams.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material3.MaterialTheme

@Composable
fun NetworkErrorComponent(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon as a placeholder for the illustration, stylized to look polished
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(32.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.WifiOff,
                contentDescription = "No Internet",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "No internet connection!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Please check Internet and click retry",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .width(160.dp)
                .height(48.dp)
        ) {
            Text(
                text = "RETRY",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
