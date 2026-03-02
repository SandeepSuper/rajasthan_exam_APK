package com.rajasthanexams.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajasthanexams.data.Test
import kotlinx.coroutines.delay

@Composable
fun LiveTestCard(
    test: Test,
    isRegistered: Boolean,
    onClick: () -> Unit,
    onRegister: () -> Unit
) {
    // Parse startsAt
    val startTime = try {
        if (test.startsAt != null) java.time.LocalDateTime.parse(test.startsAt) else null
    } catch (e: Exception) { null }
    
    val endTime = try {
        if (test.endsAt != null) java.time.LocalDateTime.parse(test.endsAt) else null
    } catch (e: Exception) { null }

    var timeRemaining by remember { mutableStateOf("") }
    val now = java.time.LocalDateTime.now()
    
    val hasStarted = if (startTime != null) now.isAfter(startTime) else true
    val isEnded = if (endTime != null) now.isAfter(endTime) else false
    
    // Timer Logic
    LaunchedEffect(test.startsAt, test.endsAt) {
        while(true) {
            val currentNow = java.time.LocalDateTime.now()
            
            if (startTime != null && currentNow.isBefore(startTime)) {
                // Not started
                val diff = java.time.Duration.between(currentNow, startTime)
                val days = diff.toDays()
                val hours = diff.toHours() % 24
                val minutes = diff.toMinutes() % 60
                
                timeRemaining = if (days > 0) "Starts in ${days}d" else "Starts in ${hours}h ${minutes}m"
            } else if (endTime != null && currentNow.isBefore(endTime)) {
                // Ongoing
                val diff = java.time.Duration.between(currentNow, endTime)
                val days = diff.toDays()
                val hours = diff.toHours() % 24
                
                timeRemaining = if (days > 0) "Ends in ${days}d" else "Ends in ${hours}h"
            } else {
                timeRemaining = "Ended"
            }
            delay(60000) // Update every minute
        }
    }

    Surface(
        shape = RoundedCornerShape(15.dp),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasStarted && !isEnded || !isRegistered) { 
               if (hasStarted && !isEnded) onClick()
               else if (!hasStarted && !isRegistered) onRegister()
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category Pill + Live Tag
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    test.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                
                // Live Tag
                Text(
                    text = "LIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color(0xFFE91E63), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title
            Text(
                text = test.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Meta Info (Icons + Text)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Text(" ${test.questions} Qs", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Text(" ${test.time} Mins", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Marks
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                Text(" ${test.questions * (test.marksPerQuestion ?: 1.0)} Marks", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha=0.5f))
            
            // Footer: Timer + Action Button Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Timer / Status
                Text(
                    text = timeRemaining,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasStarted && !isEnded) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Right: Action Text
                val buttonText = when {
                    test.isAttempted && isEnded -> "Expired"
                    test.isAttempted -> "Result" // Changed from Retake to Result as per request
                    isEnded -> "Result"
                    hasStarted -> "Start Now"
                    isRegistered -> "Registered"
                    else -> "Register"
                }
                
                val buttonColor = when {
                     test.isAttempted && isEnded -> Color.Red
                     isEnded -> MaterialTheme.colorScheme.primary
                     hasStarted -> MaterialTheme.colorScheme.secondary 
                     isRegistered -> Color(0xFF4CAF50) 
                     else -> MaterialTheme.colorScheme.primary
                }

                TextButton(
                    onClick = { 
                        if (test.isAttempted && isEnded) {
                             // do nothing (Expired)
                        } else if (test.isAttempted) {
                             onClick()
                        } else if (hasStarted && !isEnded) {
                             onClick()
                        } else if (!hasStarted && !isRegistered) {
                             onRegister()
                        }
                    },
                    enabled = !(test.isAttempted && isEnded) && (!isRegistered || hasStarted || test.isAttempted)
                ) {
                    Text(
                        text = buttonText,
                        color = if (buttonText == "Expired") Color.Red else if (isRegistered && !hasStarted && !test.isAttempted) Color(0xFF4CAF50) else if (hasStarted || test.isAttempted) Color(0xFF2196F3) else Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
