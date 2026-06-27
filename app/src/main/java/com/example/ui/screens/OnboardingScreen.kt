package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    // Smooth background gradient transition based on active page
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_glow")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // High fidelity decorative visual background (Nothing / Sci-Fi Glow)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Animated color orb elements
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x2D6366F1), Color.Transparent),
                    center = Offset(canvasWidth * 0.2f + (animOffset * 0.1f), canvasHeight * 0.3f),
                    radius = 800f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x333B82F6), Color.Transparent),
                    center = Offset(canvasWidth * 0.8f - (animOffset * 0.1f), canvasHeight * 0.7f),
                    radius = 900f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "L I F E O S",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp
                )

                TextButton(
                    onClick = { onFinished() },
                    modifier = Modifier.testTag("skip_onboarding_button")
                ) {
                    Text(
                        text = "Atla",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }

            // Slider Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingPageOne()
                    1 -> OnboardingPageTwo()
                    2 -> OnboardingPageThree()
                }
            }

            // Bottom bar with indicators and controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
                        val color by animateColorAsState(
                            targetValue = if (isSelected) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.2f)
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Next / Finish Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(32.dp),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp),
                    modifier = Modifier
                        .testTag("onboarding_action_button")
                        .height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (pagerState.currentPage == 2) "Keşfet" else "Devam Et",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageOne() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // High fidelity geometric logo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp)
                .padding(16.dp)
        ) {
            // Glowing circles
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFF3B82F6),
                    radius = size.minDimension / 2.5f,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = size.minDimension / 2.1f,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            // Inner abstract symbol
            Text(
                text = "Ω",
                fontSize = 42.sp,
                color = Color.White,
                fontWeight = FontWeight.Light
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "LIFEOS AI",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 6.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hayatını yönetmenin en akıllı yolu.",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
    }
}

@Composable
fun OnboardingPageTwo() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Cybernetic grid visualization of AI Coach
        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2, h / 2)

                // Grid lines radiating from center
                for (i in 0..12) {
                    val angle = (i * Math.PI / 6).toFloat()
                    val dx = (Math.cos(angle.toDouble()) * w).toFloat()
                    val dy = (Math.sin(angle.toDouble()) * h).toFloat()
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = center,
                        end = Offset(center.x + dx, center.y + dy),
                        strokeWidth = 1f
                    )
                }

                // AI Neural nodes
                drawCircle(Color(0xFF3B82F6), radius = 6.dp.toPx(), center = center)
                drawCircle(Color(0xFF6366F1), radius = 4.dp.toPx(), center = Offset(w * 0.3f, h * 0.4f))
                drawCircle(Color(0xFF30D158), radius = 4.dp.toPx(), center = Offset(w * 0.7f, h * 0.3f))
                drawCircle(Color(0xFFFFD60A), radius = 4.dp.toPx(), center = Offset(w * 0.75f, h * 0.7f))
                drawCircle(Color.White.copy(alpha = 0.3f), radius = 5.dp.toPx(), center = Offset(w * 0.25f, h * 0.75f))
            }

            // Glassmorphism overlay card
            GlassCard(
                modifier = Modifier
                    .width(220.dp)
                    .height(90.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AI YAŞAM KOÇU",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aktif • Analiz Ediliyor",
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Yapay zeka artık kişisel yaşam koçun.",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Gününü planlar.\nHedeflerini takip eder.\nSana özel öneriler sunar.",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun OnboardingPageThree() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success Stats custom vector drawing
        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw background grid lines
                for (i in 1..4) {
                    val y = h * (i / 5f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                // Elegant upward performance curve
                val path = Path().apply {
                    moveTo(0f, h * 0.8f)
                    cubicTo(
                        w * 0.25f, h * 0.75f,
                        w * 0.5f, h * 0.4f,
                        w * 0.75f, h * 0.35f
                    )
                    quadraticTo(
                        w * 0.9f, h * 0.3f,
                        w, h * 0.15f
                    )
                }

                drawPath(
                    path = path,
                    color = Color(0xFF30D158),
                    style = Stroke(width = 4.dp.toPx())
                )

                // Fill under the path
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x3330D158),
                            Color.Transparent
                        )
                    )
                )

                // High point highlight orb
                drawCircle(
                    color = Color(0xFF30D158),
                    radius = 6.dp.toPx(),
                    center = Offset(w, h * 0.15f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Her gün daha iyi bir versiyonun.",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hayat skorunu yükselt, disiplin puanı kazan ve hedeflerine adım adım yaklaşırken gelişimi gözlerinle gör.",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
