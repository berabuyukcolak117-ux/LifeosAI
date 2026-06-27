package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBg
import com.example.ui.theme.GlassBorder


@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    blurRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(clickableModifier)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.09f),
                        Color.White.copy(alpha = 0.03f)
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                shape = shape
            )
            .padding(16.dp),
        content = content
    )
}

@Composable
fun GlowGradientBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030303)) // Rich, cinematic black background
    ) {
        // Draw the top-left blue and bottom-right indigo blur layers using drawBehind for performance
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Top-Left Blue Glow (approx 20% alpha blue-600)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x333B82F6), Color.Transparent),
                            radius = size.width * 1.0f,
                            center = Offset(size.width * -0.1f, size.height * -0.05f)
                        )
                    )
                    // Bottom-Right Indigo Glow (approx 20% alpha indigo-600)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x336366F1), Color.Transparent),
                            radius = size.width * 0.9f,
                            center = Offset(size.width * 1.1f, size.height * 1.05f)
                        )
                    )
                }
        )
        content()
    }
}
