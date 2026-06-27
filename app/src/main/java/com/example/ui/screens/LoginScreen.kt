package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginCompleted: (String) -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("berabuyukcolak117@gmail.com") } // Pre-populate with user's email if provided
    var password by remember { mutableStateOf("password123") }
    var isSignUpMode by remember { mutableStateOf(false) }

    // Smooth backdrop animation
    val infiniteTransition = rememberInfiniteTransition(label = "login_anim")
    val radiusRatio by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radius_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Decorative glowing circle background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x2B0A84FF), Color.Transparent),
                    radius = 900f * radiusRatio,
                    center = Offset(w * 0.5f, h * 0.2f)
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x226366F1), Color.Transparent),
                    radius = 800f * radiusRatio,
                    center = Offset(w * 0.5f, h * 0.8f)
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "L I F E O S",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "PREMIUM LIFE COORDINATOR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3B82F6),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Main Login Glass Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSignUpMode) "Hesap Oluştur" else "Giriş Yap",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Email Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-posta Adresi", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Şifre", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color.White.copy(alpha = 0.6f)) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    // Error/Forgot password link
                    if (!isSignUpMode) {
                        TextButton(
                            onClick = {
                                Toast.makeText(context, "Şifre sıfırlama bağlantısı e-postanıza gönderildi (Simülasyon).", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("forgot_password_button")
                        ) {
                            Text(
                                text = "Şifremi Unuttum?",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Main Action Button (Email registration or Login)
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Lütfen tüm alanları doldurunuz.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, if (isSignUpMode) "Kayıt Başarılı!" else "Giriş Başarılı!", Toast.LENGTH_SHORT).show()
                                onLoginCompleted(email)
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
                            .testTag("submit_login_button")
                    ) {
                        Text(
                            text = if (isSignUpMode) "E-posta ile Kaydol" else "E-posta ile Giriş Yap",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.1f)))
                        Text(
                            text = "veya",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.1f)))
                    }

                    // Google Sign-In Button (High-End Styled)
                    Button(
                        onClick = {
                            Toast.makeText(context, "Google ile Giriş Yapılıyor (Firebase Auth)...", Toast.LENGTH_SHORT).show()
                            onLoginCompleted(email)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16161C),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("google_login_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // High contrast G symbol
                            Text(
                                text = "G",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0A84FF),
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "Google ile Devam Et",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Switch Mode Toggle
                    TextButton(
                        onClick = { isSignUpMode = !isSignUpMode },
                        modifier = Modifier.testTag("switch_mode_button")
                    ) {
                        Text(
                            text = if (isSignUpMode) "Zaten bir hesabın var mı? Giriş yap" else "Hesabın yok mu? Yeni hesap oluştur",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
