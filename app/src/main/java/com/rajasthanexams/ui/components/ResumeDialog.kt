package com.rajasthanexams.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ResumeDialog(
    timeLeft: String,
    attempted: Int,
    unattempted: Int,
    marked: Int,
    onResume: () -> Unit,
    onBack: () -> Unit
) {
    Dialog(onDismissRequest = onBack) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stats List
                ResumeStatItem(Icons.Default.Schedule, "Time Left", timeLeft, Color(0xFF5C6BC0))
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha=0.5f))
                ResumeStatItem(Icons.Default.CheckCircle, "Attempted", attempted.toString(), Color.Gray)
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha=0.5f))
                ResumeStatItem(Icons.Default.RemoveCircle, "Unattempted", unattempted.toString(), Color.Gray)
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha=0.5f))
                ResumeStatItem(Icons.Default.Star, "Marked", marked.toString(), Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Do you want to Resume the test?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onResume,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Resume Test", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90A4AE)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Back", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ResumeStatItem(icon: ImageVector, label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
        Text(
            value, 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold, 
            color = valueColor
        )
    }
}
