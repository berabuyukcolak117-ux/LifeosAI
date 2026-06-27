package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatMessage
import com.example.ui.LifeViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowGradientBackground
import com.example.data.Task
import com.example.data.Transaction
import com.example.data.DailyProgress
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: LifeViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val userName by viewModel.userName.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()
    val level by viewModel.level.collectAsState()
    val xpPoints by viewModel.xpPoints.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val tabs = listOf(
        TabItem("Home", Icons.Default.Home, 0),
        TabItem("AI Koç", Icons.Default.Face, 1),
        TabItem("Tasks", Icons.Default.CheckCircle, 2),
        TabItem("Fitness", Icons.Default.Favorite, 3),
        TabItem("Finance", Icons.Default.ShoppingCart, 4),
        TabItem("Stats", Icons.Default.PlayArrow, 5), // Representing progress
        TabItem("Profil", Icons.Default.Person, 6)
    )

    GlowGradientBackground {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.10f),
                                    Color.White.copy(alpha = 0.03f)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.20f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .clip(RoundedCornerShape(32.dp))
                        .navigationBarsPadding()
                ) {
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab.index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab.index },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFF3B82F6).copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.title.lowercase()}")
                        )
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "dashboard_tabs"
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> HomeScreen(viewModel, userName, streakCount, level, xpPoints)
                        1 -> AICoachScreen(viewModel)
                        2 -> TasksScreen(viewModel)
                        3 -> FitnessScreen(viewModel)
                        4 -> FinanceScreen(viewModel)
                        5 -> StatisticsScreen(viewModel)
                        6 -> ProfileScreen(viewModel, userName, isPremium, cloudSyncStatus, level, xpPoints)
                    }
                }
            }
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector, val index: Int)

// ==========================================
// 1. HOME SCREEN SECTION
// ==========================================
@Composable
fun HomeScreen(
    viewModel: LifeViewModel,
    userName: String,
    streakCount: Int,
    level: Int,
    xpPoints: Int
) {
    val tasks by viewModel.currentDayTasks.collectAsState(emptyList())
    val progress by viewModel.currentDayProgress.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val context = LocalContext.current

    // Formatting date neatly
    val formattedDate = remember(selectedDate) {
        val localDate = LocalDate.parse(selectedDate)
        val dayOfWeek = localDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("tr"))
        val dayOfMonth = localDate.dayOfMonth
        val month = localDate.month.getDisplayName(TextStyle.FULL, Locale("tr"))
        "$dayOfMonth $month, $dayOfWeek"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Upper Greeting Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Günaydın $userName ☀️",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                // Level / XP Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF16161C))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Lvl $level",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD60A)
                        )
                        Text(
                            text = "$xpPoints XP",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Horizontal Calendar Strip
        item {
            CalendarStrip(selectedDate) { viewModel.selectDate(it) }
        }

        // Daily Score Circle Card
        item {
            val completedCount = tasks.count { it.isCompleted }
            val totalCount = tasks.size
            val taskRatio = if (totalCount > 0) completedCount.toFloat() / totalCount else 0.5f
            val waterRatio = (progress?.waterMl ?: 1000).toFloat() / 2500f
            val stepsRatio = (progress?.stepsCount ?: 5000).toFloat() / 10000f
            val lifeScore = ((taskRatio + waterRatio + stepsRatio) / 3f * 100).roundToInt().coerceIn(0..100)

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Custom concentric status charts
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Circular track
                            drawCircle(
                                color = Color.White.copy(alpha = 0.05f),
                                radius = size.minDimension / 2.3f,
                                style = Stroke(width = 8.dp.toPx())
                            )
                            // Progress sweep
                            drawArc(
                                color = Color(0xFF3B82F6),
                                startAngle = -90f,
                                sweepAngle = (lifeScore / 100f) * 360f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx()),
                                size = Size(size.width * 0.86f, size.height * 0.86f),
                                topLeft = Offset(size.width * 0.07f, size.height * 0.07f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%$lifeScore",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Hayat Skoru",
                                fontSize = 8.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Günün Analizi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lifeScore > 75) "$userName, harika bir disiplin sergiliyorsun!" 
                                   else "$userName, bugün hedeflerine odaklanmak için iyi bir gün.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "🔥 $streakCount Günlük Seri",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD60A)
                            )
                            Text(
                                text = "🎯 $completedCount / $totalCount Görev",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF30D158)
                            )
                        }
                    }
                }
            }
        }

        // AI Advice Dynamic Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_advice_card")
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF30D158).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "AI Advice",
                            tint = Color(0xFF30D158),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "KİŞİSEL AI KOÇ ÖNERİSİ",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF30D158),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$userName, bugünkü ana hedefin: \"Kas yapmak\". Günlük protein alımına ve su tüketimine dikkat etmelisin. Spor salonunda görüşmek üzere!",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Metrics Grid (Water, Steps, Calories, Focus Duration)
        item {
            Text(
                text = "Günlük Takip",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Water intake tracking Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F1E36))
                        .border(1.dp, Color(0xFF0A84FF).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel.addWater(250)
                            Toast.makeText(context, "250 ml su eklendi! 💧", Toast.LENGTH_SHORT).show()
                        }
                        .padding(16.dp)
                        .testTag("metric_water_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Su", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                            Icon(Icons.Default.AddCircle, contentDescription = "Add Water", tint = Color(0xFF0A84FF), modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "${progress?.waterMl ?: 750} / 2500 ml",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        LinearProgressIndicator(
                            progress = { ((progress?.waterMl ?: 750) / 2500f).coerceIn(0f, 1f) },
                            color = Color(0xFF0A84FF),
                            trackColor = Color(0xFF0A84FF).copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                        )
                    }
                }

                // Step count logging card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F261D))
                        .border(1.dp, Color(0xFF30D158).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel.addSteps(1000)
                            Toast.makeText(context, "1000 adım atıldı! 🏃", Toast.LENGTH_SHORT).show()
                        }
                        .padding(16.dp)
                        .testTag("metric_steps_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Adım", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                            Icon(Icons.Default.Add, contentDescription = "Add steps", tint = Color(0xFF30D158), modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "${progress?.stepsCount ?: 4320} / 10000",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        LinearProgressIndicator(
                            progress = { ((progress?.stepsCount ?: 4320) / 10000f).coerceIn(0f, 1f) },
                            color = Color(0xFF30D158),
                            trackColor = Color(0xFF30D158).copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calories burned logging card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF131824))
                        .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Kalori", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        Text(
                            text = "${progress?.caloriesBurned ?: 180} kcal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Hedef: 2500 kcal",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }

                // Focus timer adding card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF24162B))
                        .border(1.dp, Color(0xFFBF5AF2).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel.addFocusMinutes(15)
                            Toast.makeText(context, "+15 Dakika Odaklanma Başlatıldı! 🎯", Toast.LENGTH_SHORT).show()
                        }
                        .padding(16.dp)
                        .testTag("metric_focus_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Odaklanma Süresi", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        Text(
                            text = "${progress?.focusMinutes ?: 25} dk",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "+15 dk Ekle 🎯",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBF5AF2)
                        )
                    }
                }
            }
        }

        // Recent Task completed lists
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Günün Planı",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        if (tasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bugün için bir görev planlanmamış.",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(tasks.take(4)) { task ->
                TaskRowItem(task, onChecked = { viewModel.toggleTask(task) })
            }
        }
    }
}

@Composable
fun CalendarStrip(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val days = remember {
        val today = LocalDate.now()
        (0..6).map { today.plusDays(it.toLong() - 2) }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(days) { date ->
            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val isSelected = selectedDate == dateString
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("tr"))
            val dayNumber = date.dayOfMonth.toString()

            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) Color.White else Color(0xFF16161A))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onDateSelected(dateString) }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dayName,
                        fontSize = 10.sp,
                        color = if (isSelected) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayNumber,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun TaskRowItem(
    task: Task,
    onChecked: () -> Unit
) {
    val categoryColor = when (task.priority) {
        "High" -> Color(0xFFFF3037)
        "Medium" -> Color(0xFF0A84FF)
        else -> Color(0xFF30D158)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF121214))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onChecked() },
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF30D158),
                uncheckedColor = Color.White.copy(alpha = 0.4f)
            ),
            modifier = Modifier.testTag("task_check_${task.id}")
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (task.isCompleted) Color.White.copy(alpha = 0.4f) else Color.White,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.description,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }

        // Category Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(categoryColor.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = task.category,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = categoryColor
            )
        }
    }
}

// ==========================================
// 2. AI COACH CHAT SCREEN SECTION
// ==========================================
@Composable
fun AICoachScreen(viewModel: LifeViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isAiThinking.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Upper Title Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF30D158))
            )
            Text(
                text = "L I F E O S   A I   K O Ç",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )
        }

        // Chat Bubble lazy column
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                val alignment = if (msg.isUser) Alignment.End else Alignment.Start
                val cardBg = if (msg.isUser) Color(0xFF1E1E24) else Color(0x331E1E24)
                val cardBorder = if (msg.isUser) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = alignment
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = if (msg.isUser) 20.dp else 4.dp,
                                bottomEnd = if (msg.isUser) 4.dp else 20.dp
                            ))
                            .background(cardBg)
                            .border(
                                1.dp,
                                cardBorder,
                                RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = if (msg.isUser) 20.dp else 4.dp,
                                    bottomEnd = if (msg.isUser) 4.dp else 20.dp
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Text(
                            text = msg.text,
                            fontSize = 13.sp,
                            color = Color.White,
                            lineHeight = 20.sp
                        )
                    }
                    Text(
                        text = if (msg.isUser) "Sen" else "LifeOS AI",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
                    )
                }
            }

            if (isThinking) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Koç düşünüyor...",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Input Actions Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Upload Photo / Attachment Mockup Button
            IconButton(
                onClick = { Toast.makeText(context, "Fotoğraf analizi ve PDF yükleme özelliği simüle edildi. (Masaüstü/Galeri entegrasyonu)", Toast.LENGTH_LONG).show() },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF16161A)),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Upload", tint = Color.White)
            }

            // Input Field
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("AI Koç ile konuş...", color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.2f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color(0xFF16161C),
                    unfocusedContainerColor = Color(0xFF16161C)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("ai_chat_input"),
                singleLine = true
            )

            // Voice recorder/Send Toggle Button
            IconButton(
                onClick = {
                    if (inputQuery.isNotBlank()) {
                        viewModel.sendChatMessage(inputQuery)
                        inputQuery = ""
                    } else {
                        Toast.makeText(context, "Ses kaydediliyor... (Simülasyon)", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White),
                modifier = Modifier.size(48.dp).testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = if (inputQuery.isNotBlank()) Icons.Default.Send else Icons.Default.PlayArrow,
                    contentDescription = "Send/Voice",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ==========================================
// 3. TASKS MANAGEMENT SECTION
// ==========================================
@Composable
fun TasksScreen(viewModel: LifeViewModel) {
    val tasks by viewModel.allTasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDesc by remember { mutableStateOf("") }
    var newTaskCategory by remember { mutableStateOf("Personal") }
    var newTaskPriority by remember { mutableStateOf("Medium") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GÖREV YÖNETİMİ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_task_trigger")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ekle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Planlanmış hiçbir görev yok. Ekleyerek başla!",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tasks) { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF121214))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { viewModel.toggleTask(task) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF30D158),
                                uncheckedColor = Color.White.copy(alpha = 0.4f)
                            )
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (task.isCompleted) Color.White.copy(alpha = 0.4f) else Color.White,
                            )
                            if (task.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = task.description,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.5f),
                                )
                            }
                        }

                        // Priority/Delete icons
                        IconButton(
                            onClick = { viewModel.removeTask(task) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Yeni Görev Planla", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Görev Adı", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_task_title")
                    )

                    OutlinedTextField(
                        value = newTaskDesc,
                        onValueChange = { newTaskDesc = it },
                        label = { Text("Açıklama (Opsiyonel)", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Priority selection
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Low", "Medium", "High").forEach { pr ->
                            val isSelected = newTaskPriority == pr
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color.White else Color(0xFF1C1C20))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { newTaskPriority = pr }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pr,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Category selection
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Personal", "Work", "Health", "Finance").forEach { cat ->
                            val isSelected = newTaskCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color.White else Color(0xFF1C1C20))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { newTaskCategory = cat }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addTask(newTaskTitle, newTaskDesc, newTaskCategory, newTaskPriority)
                            newTaskTitle = ""
                            newTaskDesc = ""
                            showAddDialog = false
                            Toast.makeText(context, "Görev başarıyla eklendi! 🎯", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.testTag("add_task_submit")
                ) {
                    Text("Oluştur")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("İptal", color = Color.White.copy(alpha = 0.6f))
                }
            },
            containerColor = Color(0xFF121216)
        )
    }
}

// ==========================================
// 4. FITNESS & HEALTH SCREEN SECTION
// ==========================================
@Composable
fun FitnessScreen(viewModel: LifeViewModel) {
    val progress by viewModel.currentDayProgress.collectAsState()
    val context = LocalContext.current
    var selectedWorkout by remember { mutableStateOf("") }

    val workouts = listOf(
        "Chest & Triceps (Göğüs & Arka Kol)",
        "Back & Biceps (Sırt & Ön Kol)",
        "Legs & Core (Bacak & Karın)",
        "Cardio & HIIT (Kardiyo)",
        "Yoga & Stretching (Esnetme)"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "SPOR VE SAĞLIK TAKİBİ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )
        }

        // Concentric circular muscular canvas guide
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AKTİF KAS GRUPLARI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Box(
                        modifier = Modifier
                            .height(150.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val center = Offset(w / 2, h / 2)

                            // Drawn Abstract human body mapping guide in Nothing OS style
                            drawCircle(Color.White.copy(alpha = 0.1f), radius = 24.dp.toPx(), center = center)
                            drawCircle(Color.White.copy(alpha = 0.05f), radius = 48.dp.toPx(), center = center)
                            drawCircle(Color.White.copy(alpha = 0.02f), radius = 72.dp.toPx(), center = center)

                            // Draw central body lines
                            drawLine(Color.White.copy(alpha = 0.2f), start = Offset(w/2, h*0.2f), end = Offset(w/2, h*0.8f))
                            drawLine(Color.White.copy(alpha = 0.1f), start = Offset(w*0.3f, h/2), end = Offset(w*0.7f, h/2))

                            // Draw muscular nodes
                            drawCircle(Color(0xFF3B82F6), radius = 8f, center = Offset(w/2, h*0.35f)) // Core
                            drawCircle(Color(0xFF0A84FF), radius = 6f, center = Offset(w*0.43f, h*0.3f)) // Left chest
                            drawCircle(Color(0xFF0A84FF), radius = 6f, center = Offset(w*0.57f, h*0.3f)) // Right chest
                            drawCircle(Color(0xFF30D158), radius = 6f, center = Offset(w*0.42f, h*0.6f)) // Left quadricep
                            drawCircle(Color(0xFF30D158), radius = 6f, center = Offset(w*0.58f, h*0.6f)) // Right quadricep
                        }
                    }
                    Text(
                        text = "Core: Yoğun • Göğüs: Orta • Bacaklar: Düşük",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Active workouts
        item {
            Text(
                text = "Bugünkü Antrenman Programı",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        items(workouts) { workout ->
            val isLogged = progress?.activeWorkoutType == workout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isLogged) Color(0xFF0F261D) else Color(0xFF121214))
                    .border(
                        1.dp,
                        if (isLogged) Color(0xFF30D158).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        viewModel.logWorkout(workout)
                        Toast.makeText(context, "$workout kaydedildi! +50 XP 🏋️", Toast.LENGTH_SHORT).show()
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = workout,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLogged) Color(0xFF30D158) else Color.White
                )

                if (isLogged) {
                    Icon(Icons.Default.Check, contentDescription = "Logged", tint = Color(0xFF30D158), modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text = "+50 XP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. FINANCE MANAGEMENT SECTION
// ==========================================
@Composable
fun FinanceScreen(viewModel: LifeViewModel) {
    val transactions by viewModel.allTransactions.collectAsState()
    var amountText by remember { mutableStateOf("") }
    var descText by remember { mutableStateOf("") }
    var isIncomeSelected by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Gıda") }
    val context = LocalContext.current

    val categories = listOf("Maaş", "Yatırım", "Gıda", "Eğlence", "Kira", "Fatura", "Diğer")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "FİNANSAL ANALİZ VE BÜTÇE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )
        }

        // Finance Balance Header Card
        item {
            val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
            val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }
            val balance = totalIncome - totalExpense

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Toplam Net Bakiye",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format(Locale.US, "%,.2f", balance)} ₺",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = if (balance >= 0) Color(0xFF30D158) else Color(0xFFFF3037)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Gelir", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                            Text("+${String.format(Locale.US, "%,.2f", totalIncome)} ₺", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF30D158))
                        }
                        Column {
                            Text("Gider", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                            Text("-${String.format(Locale.US, "%,.2f", totalExpense)} ₺", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF3037))
                        }
                    }
                }
            }
        }

        // Add transaction sheet
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "İşlem Kaydet",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Income / Expense selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isIncomeSelected) Color(0xFFFF3037) else Color(0xFF16161A))
                                .clickable { isIncomeSelected = false }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Gider", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isIncomeSelected) Color(0xFF30D158) else Color(0xFF16161A))
                                .clickable { isIncomeSelected = true }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Gelir", color = if (isIncomeSelected) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Txt Inputs
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Tutar (₺)", color = Color.White.copy(alpha = 0.5f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("finance_amount_input")
                    )

                    OutlinedTextField(
                        value = descText,
                        onValueChange = { descText = it },
                        label = { Text("Açıklama", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category horizontal row scroll
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color.White else Color(0xFF1C1C20))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                viewModel.addTransaction(amt, isIncomeSelected, selectedCategory, descText)
                                amountText = ""
                                descText = ""
                                Toast.makeText(context, "İşlem başarıyla kaydedildi! 💳", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Geçersiz Tutar", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("finance_add_submit")
                    ) {
                        Text("Ekle", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Transactions list section
        item {
            Text(
                text = "İşlem Geçmişi",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (transactions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Text("Hiçbir finans işlemi kaydedilmemiş.", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                }
            }
        } else {
            items(transactions) { tx ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF121214))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (tx.isIncome) Color(0xFF30D158).copy(alpha = 0.15f) else Color(0xFFFF3037).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (tx.isIncome) Icons.Default.Add else Icons.Default.PlayArrow, // Outward representation
                                contentDescription = null,
                                tint = if (tx.isIncome) Color(0xFF30D158) else Color(0xFFFF3037),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(text = tx.description.ifBlank { tx.category }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = tx.category, fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "${if (tx.isIncome) "+" else "-"}${tx.amount} ₺",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = if (tx.isIncome) Color(0xFF30D158) else Color(0xFFFF3037)
                        )

                        IconButton(onClick = { viewModel.removeTransaction(tx) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. STATISTICS SCREEN SECTION
// ==========================================
@Composable
fun StatisticsScreen(viewModel: LifeViewModel) {
    val tasks by viewModel.allTasks.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val progressList by viewModel.allProgress.collectAsState(emptyList())

    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val taskCompRatio = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks * 100).roundToInt() else 65

    val averageSteps = if (progressList.isNotEmpty()) progressList.map { it.stepsCount }.average().roundToInt() else 5430
    val totalFocusMins = if (progressList.isNotEmpty()) progressList.sumOf { it.focusMinutes } else 125

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "PERFORMANS VE İSTATİSTİK",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )
        }

        // Summary metrics
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Discipline Score Card
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("Disiplin Skoru", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                        Text("%$taskCompRatio", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF30D158))
                        Text("Görev tamamlama oranı", fontSize = 9.sp, color = Color.White.copy(alpha = 0.4f))
                    }
                }

                // Focus metrics Card
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("Odaklanma", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                        Text("${totalFocusMins} dk", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFFBF5AF2))
                        Text("Toplam çalışma süresi", fontSize = 9.sp, color = Color.White.copy(alpha = 0.4f))
                    }
                }
            }
        }

        // Focus hours Line graph drawn on custom Canvas
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HAFTALIK ODAKLANMA ANALİZİ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBF5AF2),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .height(140.dp)
                            .fillMaxWidth()
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw baseline grid lines
                            drawLine(Color.White.copy(alpha = 0.05f), Offset(0f, h*0.25f), Offset(w, h*0.25f))
                            drawLine(Color.White.copy(alpha = 0.05f), Offset(0f, h*0.5f), Offset(w, h*0.5f))
                            drawLine(Color.White.copy(alpha = 0.05f), Offset(0f, h*0.75f), Offset(w, h*0.75f))

                            // Draw statistical graph curve representing weekly progression
                            val graphPoints = listOf(
                                Offset(w * 0.1f, h * 0.8f),
                                Offset(w * 0.25f, h * 0.45f),
                                Offset(w * 0.4f, h * 0.6f),
                                Offset(w * 0.55f, h * 0.25f),
                                Offset(w * 0.7f, h * 0.5f),
                                Offset(w * 0.85f, h * 0.3f),
                                Offset(w * 0.95f, h * 0.15f)
                            )

                            val path = Path().apply {
                                moveTo(graphPoints[0].x, graphPoints[0].y)
                                for (i in 1 until graphPoints.size) {
                                    lineTo(graphPoints[i].x, graphPoints[i].y)
                                }
                            }

                            drawPath(
                                path = path,
                                color = Color(0xFFBF5AF2),
                                style = Stroke(width = 3.dp.toPx())
                            )

                            // Under-curve gradient flow
                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo(graphPoints.last().x, h)
                                lineTo(graphPoints.first().x, h)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0x33BF5AF2), Color.Transparent)
                                )
                            )

                            // Dots
                            graphPoints.forEach { pt ->
                                drawCircle(Color(0xFFBF5AF2), radius = 4.dp.toPx(), center = pt)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz").forEach { day ->
                            Text(day, fontSize = 9.sp, color = Color.White.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }

        // Health average analytics
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Sağlık ve Fitness Ortalamaları",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Günlük Ortalama Adım", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("$averageSteps adım / gün", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF30D158))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Su Hedefi Ulaşım Oranı", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("%82", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A84FF))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Kalori Yakımı", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("Ort. 420 kcal / gün", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF3037))
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. PROFILE AND PREMIUM SETTINGS SECTION
// ==========================================
@Composable
fun ProfileScreen(
    viewModel: LifeViewModel,
    name: String,
    isPremium: Boolean,
    cloudSyncStatus: String,
    level: Int,
    xp: Int
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "KULLANICI PROFİLİ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )
        }

        // Profile details Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1C1C22))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    Column {
                        Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFD60A).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Lvl $level", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD60A))
                            }

                            if (isPremium) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Premium", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                                }
                            }
                        }
                    }
                }
            }
        }

        // VIP/Premium Subscription card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = if (isPremium) listOf(Color(0xFF30D158), Color(0xFF0A84FF))
                                     else listOf(Color(0xFF3B82F6), Color(0xFFBF5AF2))
                        )
                    )
                    .clickable {
                        viewModel.togglePremiumStatus()
                        Toast.makeText(
                            context,
                            if (isPremium) "Premium üyelik sonlandırıldı." else "Tebrikler! Premium LifeOS AI üyesi oldunuz! 💎",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .padding(20.dp)
                    .testTag("premium_upgrade_card")
            ) {
                Column {
                    Text(
                        text = if (isPremium) "LIFEOS AI PREMIUM AKTİF" else "LIFEOS AI PREMIUM'A YÜKSELT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isPremium) "Kişisel yaşam koçunuz en derin analizleriyle hizmetinizde."
                               else "%100 Kişiselleştirilmiş AI Koç, Sınırsız Raporlama ve Özel Temalar için Dokun.",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Settings option panel (Sync status, export data, privacy, notifications)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Uygulama Ayarları", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    // Cloud Sync action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.triggerCloudSync()
                                Toast.makeText(context, "Bulut senkronizasyonu başlatıldı...", Toast.LENGTH_SHORT).show()
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            Text("Bulut Senkronizasyonu", fontSize = 13.sp, color = Color.White)
                        }
                        Text(
                            text = cloudSyncStatus,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cloudSyncStatus == "Synced") Color(0xFF30D158) else Color(0xFFFF3037)
                        )
                    }

                    // Theme choice mockup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { Toast.makeText(context, "Minimalist Dark Tema Aktif", Toast.LENGTH_SHORT).show() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            Text("Görsel Tema Seçimi", fontSize = 13.sp, color = Color.White)
                        }
                        Text("Minimal Dark", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                    }

                    // Data export mockup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { Toast.makeText(context, "Veriler JSON formatında dışa aktarıldı. (lifeos_export.json)", Toast.LENGTH_LONG).show() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            Text("Verilerimi Dışa Aktar", fontSize = 13.sp, color = Color.White)
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Logout Button
        item {
            Button(
                onClick = { viewModel.performLogout() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261616), contentColor = Color(0xFFFF3037)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("logout_button")
            ) {
                Text("Çıkış Yap", fontWeight = FontWeight.Bold)
            }
        }
    }
}
