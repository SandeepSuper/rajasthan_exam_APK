package com.rajasthanexams.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rajasthanexams.data.Test
import kotlinx.coroutines.delay

@Composable
fun LiveTestBanner(
    test: Test, 
    isRegistered: Boolean, 
    onClick: () -> Unit, 
    onRegister: () -> Unit
) {
    // Parse startsAt
    val startTime = try {
        if (test.startsAt != null) java.time.LocalDateTime.parse(test.startsAt) else null
    } catch (e: Exception) { null }
    
    var timeRemaining by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val hasStarted = remember(timeRemaining) { 
        timeRemaining == "Started" || timeRemaining == "Live Now" 
    }
    
    // Timer Logic
    LaunchedEffect(test.startsAt) {
        if (startTime != null) {
            while(true) {
                val now = java.time.LocalDateTime.now()
                val diff = java.time.Duration.between(now, startTime)
                
                if (diff.isNegative) {
                    timeRemaining = "Started"
                } else {
                    val days = diff.toDays()
                    val hours = diff.toHours() % 24
                    val minutes = diff.toMinutes() % 60
                    val seconds = diff.seconds % 60
                    
                    timeRemaining = if (days > 0) {
                        "Starts in: ${days}d ${hours}h"
                    } else if (hours > 0) {
                        "Starts in: ${hours}h ${minutes}m"
                    } else {
                        "Starts in: ${minutes}m ${seconds}s"
                    }
                }
                delay(1000)
            }
        } else {
            timeRemaining = "Live Now"
        }
    }

    val cardColor = if (!hasStarted && isRegistered) Color.Gray else Color(0xFFD32F2F)

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor), // Red Urgent or Gray if registered
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasStarted || !isRegistered) { 
                if (hasStarted) onClick() 
                else {
                    onRegister()
                    android.widget.Toast.makeText(context, "Registered successfully! You will be notified.", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blinking Dot
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            if (hasStarted) {
                Box(modifier = Modifier.size(12.dp).background(Color.White.copy(alpha=alpha), CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            Column {
                Text(
                    if (hasStarted) "🔥 LIVE TEST" else if (isRegistered) "✅ REGISTERED" else "📅 UPCOMING LIVE TEST",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    test.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    timeRemaining,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFEB3B), // Yellow for visibility
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { 
                    if (hasStarted) onClick() 
                    else {
                        onRegister()
                        android.widget.Toast.makeText(context, "Registered successfully! You will be notified.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = hasStarted || !isRegistered,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    if (hasStarted) "Join" else if (isRegistered) "Registered" else "Register", 
                    color = if (hasStarted || !isRegistered) Color(0xFFD32F2F) else Color.DarkGray, 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
