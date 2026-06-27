package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LifeViewModel
import com.example.ui.components.GlassCard
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileWizardScreen(
    viewModel: LifeViewModel,
    onWizardCompleted: () -> Unit
) {
    val context = LocalContext.current
    val currentStep by viewModel.wizardStep.collectAsState()
    
    // Step inputs
    val name by viewModel.wizardName.collectAsState()
    val birthYear by viewModel.wizardBirthYear.collectAsState()
    val height by viewModel.wizardHeight.collectAsState()
    val weight by viewModel.wizardWeight.collectAsState()
    val selectedGoal by viewModel.wizardGoal.collectAsState()
    val notificationsEnabled by viewModel.wizardNotifications.collectAsState()
    val profilePhoto by viewModel.wizardPhoto.collectAsState()

    // Confetti and floating items animation for Step 8
    val transition = rememberInfiniteTransition(label = "celebration")
    val confettiOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Glowing orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x110A84FF), Color.Transparent),
                    radius = 900f
                ),
                center = Offset(size.width * 0.1f, size.height * 0.1f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x156366F1), Color.Transparent),
                    radius = 1000f
                ),
                center = Offset(size.width * 0.9f, size.height * 0.9f)
            )
        }

        // Confetti Canvas (Step 8 only)
        if (currentStep == 8) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val confettiColors = listOf(
                    Color(0xFFFF3037), Color(0xFF0A84FF), Color(0xFF30D158), Color(0xFFFFD60A)
                )
                for (i in 0..15) {
                    val x = (w * (i / 15f) + confettiOffset * (i % 3 - 1)) % w
                    val y = (confettiOffset * (1.2f + (i % 2) * 0.3f) + h * (i / 15f)) % h
                    drawCircle(
                        color = confettiColors[i % confettiColors.size],
                        radius = 8f,
                        center = Offset(x, y)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step indicator progress bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profil Kurulumu",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$currentStep / 8",
                        color = Color(0xFF3B82F6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    val fillFraction = currentStep / 8f
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fillFraction)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF6366F1), Color(0xFF3B82F6))
                                )
                            )
                    )
                }
            }

            // Main content section containing interactive screens based on step
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "step_content"
                ) { step ->
                    when (step) {
                        1 -> StepOne(name) { viewModel.wizardName.value = it }
                        2 -> StepTwo(profilePhoto) { viewModel.wizardPhoto.value = it }
                        3 -> StepThree(birthYear) { viewModel.wizardBirthYear.value = it }
                        4 -> StepFour(height) { viewModel.wizardHeight.value = it }
                        5 -> StepFive(weight) { viewModel.wizardWeight.value = it }
                        6 -> StepSix(selectedGoal) { viewModel.setWizardGoalChoice(it) }
                        7 -> StepSeven(notificationsEnabled) { viewModel.toggleWizardNotification(it) }
                        8 -> StepEight(name)
                    }
                }
            }

            // Bottom Actions Bar
            Button(
                onClick = {
                    if (currentStep == 1 && name.isBlank()) {
                        Toast.makeText(context, "Lütfen isminizi giriniz.", Toast.LENGTH_SHORT).show()
                    } else if (currentStep < 8) {
                        viewModel.advanceWizard()
                    } else {
                        viewModel.advanceWizard() // saves state to repo
                        onWizardCompleted()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("wizard_next_button")
            ) {
                Text(
                    text = if (currentStep == 8) "Başlayalım!" else "Devam Et",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun StepOne(name: String, onNameChange: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Merhaba 👋",
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sana nasıl hitap etmeliyim?",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("İsminiz...", color = Color.White.copy(alpha = 0.3f)) },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFF3B82F6),
                unfocusedIndicatorColor = Color.White.copy(alpha = 0.2f),
                cursorColor = Color(0xFF3B82F6)
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .testTag("wizard_name_input")
        )
    }
}

@Composable
fun StepTwo(photoUri: String?, onPhotoSelect: (String?) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Seni Tanıyalım",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bir profil fotoğrafı ekle veya bu adımı atla.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFF16161A))
                .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Avatar",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onPhotoSelect("kamera") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24)),
                modifier = Modifier.testTag("wizard_photo_camera")
            ) {
                Text("Kamera")
            }
            Button(
                onClick = { onPhotoSelect("galeri") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24)),
                modifier = Modifier.testTag("wizard_photo_gallery")
            ) {
                Text("Galeri")
            }
            TextButton(
                onClick = { onPhotoSelect(null) },
                modifier = Modifier.testTag("wizard_photo_skip")
            ) {
                Text("Atla", color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun StepThree(year: Int, onYearChange: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Doğum Yılın?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = year.toString(),
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF3B82F6)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Slider(
            value = year.toFloat(),
            onValueChange = { onYearChange(it.roundToInt()) },
            valueRange = 1950f..2015f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF3B82F6),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .testTag("wizard_year_slider")
        )
    }
}

@Composable
fun StepFour(height: Float, onHeightChange: (Float) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Boyunuz?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "${height.roundToInt()} cm",
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0A84FF)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Slider(
            value = height,
            onValueChange = onHeightChange,
            valueRange = 120f..220f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF0A84FF),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .testTag("wizard_height_slider")
        )
    }
}

@Composable
fun StepFive(weight: Float, onWeightChange: (Float) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Kilonuz?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "${weight.roundToInt()} kg",
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF30D158)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Slider(
            value = weight,
            onValueChange = onWeightChange,
            valueRange = 40f..150f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF30D158),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .testTag("wizard_weight_slider")
        )
    }
}

@Composable
fun StepSix(selectedGoal: String, onGoalSelect: (String) -> Unit) {
    val goals = listOf(
        "Kas yapmak", "Kilo vermek", "Sağlıklı yaşamak",
        "Daha üretken olmak", "Ders çalışmak", "Para biriktirmek"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Ana Hedefin Nedir?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sana özel plan hazırlarken bu hedefi önceliklendireceğiz.",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            goals.chunked(2).forEach { pair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pair.forEach { goal ->
                        val isSelected = selectedGoal == goal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color.White else Color(0xFF16161A))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onGoalSelect(goal) }
                                .padding(12.dp)
                                .testTag("wizard_goal_$goal"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = goal,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepSeven(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Hatırlatıcılar",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Su içmeyi, egzersiz yapmayı veya görevlerini gün içinde kaçırmaman için bildirim izinlerini aktif edelim.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(40.dp))

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bildirimleri Aktif Et",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color.White,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                        uncheckedTrackColor = Color(0xFF1E1E24)
                    ),
                    modifier = Modifier.testTag("wizard_notification_switch")
                )
            }
        }
    }
}

@Composable
fun StepEight(name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF30D158)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Harika $name!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Artık seni tanıyorum.\nHayatını birlikte yöneteceğiz.",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )
    }
}
