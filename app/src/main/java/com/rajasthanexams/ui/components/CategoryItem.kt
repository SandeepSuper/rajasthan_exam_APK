package com.rajasthanexams.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rajasthanexams.data.Category

@Composable
fun CategoryItem(category: Category, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    ) {
        Box {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                    if (!category.iconUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = category.iconUrl,
                            contentDescription = category.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.title,
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Premium/Lock Badge
            if (category.isPremium) {
                Surface(
                    shape = CircleShape,
                    color = if (category.isPurchased) Color(0xFF43A047) else Color(0xFFE53935), // Green if purchased, Red if locked
                    modifier = Modifier.align(Alignment.TopEnd).size(20.dp).offset(x = 4.dp, y = (-4).dp),
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                             imageVector = if (category.isPurchased) Icons.Default.CheckCircle else Icons.Default.Security, // Or Lock icon if available
                             contentDescription = "Premium",
                             tint = Color.White,
                             modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            category.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        
        if (category.isPremium && !category.isPurchased) {
            Text(
                "₹${category.price.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                "${category.testsAvailable} Tests",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
