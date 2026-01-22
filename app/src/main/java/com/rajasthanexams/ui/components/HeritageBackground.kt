package com.rajasthanexams.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun HeritagePatternBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Subtle background pattern drawing
        val patternColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val step = 60.dp.toPx()
            
            // Draw simple geometric pattern inspired by Jaali work
            for (x in 0..width.toInt() step step.toInt()) {
                for (y in 0..height.toInt() step step.toInt()) {
                    drawCircle(
                        color = patternColor,
                        radius = 2.dp.toPx(),
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                    
                    // Diamond shape hints
                   if ((x / step.toInt()) % 2 == 0) {
                       drawLine(
                           color = patternColor,
                           start = Offset(x.toFloat(), y.toFloat() - 10f),
                           end = Offset(x.toFloat(), y.toFloat() + 10f),
                           strokeWidth = 1.dp.toPx()
                       )
                       drawLine(
                           color = patternColor,
                           start = Offset(x.toFloat() - 10f, y.toFloat()),
                           end = Offset(x.toFloat() + 10f, y.toFloat()),
                           strokeWidth = 1.dp.toPx()
                       )
                   }
                }
            }
        }
        
        // Content overlay
        content()
    }
}
