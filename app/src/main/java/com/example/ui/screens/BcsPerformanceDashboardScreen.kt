package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.PlannerHistoryItem
import com.example.ui.viewmodel.QuizHistoryItem
import com.example.ui.viewmodel.VivaHistoryItem
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BcsPerformanceDashboardScreen(
    vivaHistory: List<VivaHistoryItem>,
    quizHistory: List<QuizHistoryItem>,
    plannerHistory: List<PlannerHistoryItem>,
    bookmarkedQuestions: List<com.example.data.models.QuestionEntity> = emptyList(),
    onToggleBookmark: (String) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTabIdx by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("performance_dashboard_container"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "৩০ দিনের পারফরম্যান্স ড্যাশবোর্ড",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("dashboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "ফিরে চলুন",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Elegant Tab selector
            TabRow(
                selectedTabIndex = activeTabIdx,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().testTag("dashboard_tab_row")
            ) {
                Tab(
                    selected = activeTabIdx == 0,
                    onClick = { activeTabIdx = 0 },
                    text = { Text("🎤 মক ভাইভা", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_viva")
                )
                Tab(
                    selected = activeTabIdx == 1,
                    onClick = { activeTabIdx = 1 },
                    text = { Text("📝 কুইজ প্র্যাকটিস", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_quiz")
                )
                Tab(
                    selected = activeTabIdx == 2,
                    onClick = { activeTabIdx = 2 },
                    text = { Text("📅 রুটিন প্রগতি", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_planner")
                )
            }

            AnimatedContent(
                targetState = activeTabIdx,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { targetIdx ->
                when (targetIdx) {
                    0 -> VivaAnalyticsTab(vivaHistory)
                    1 -> QuizAnalyticsTab(quizHistory, bookmarkedQuestions, onToggleBookmark)
                    2 -> PlannerAnalyticsTab(plannerHistory)
                }
            }
        }
    }
}

// ==========================================
// TAB 1: MOCK VIVA ANALYTICS (GRADIENT AREA CHART WITH DUSTY DOTS & FLOATING TOOLTIP)
// ==========================================
@Composable
fun VivaAnalyticsTab(historyList: List<VivaHistoryItem>) {
    val totalBoards = historyList.size
    val averageScore = if (historyList.isEmpty()) 0 else historyList.map { it.score }.average().toInt()
    val maxScore = if (historyList.isEmpty()) 0 else historyList.maxOf { it.score }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            // Header description and summary metric boxes
            Text(
                text = "বিসিএস মক ভাইভা বোর্ড ট্র্যাকিং",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "বিগত ৩০ দিনের মধ্যে অনুষ্ঠিত মক ভাইভায় প্রাপ্ত নম্বরের ধারাবাহিক প্রগতি এখানে প্রদর্শিত হচ্ছে। পাস নম্বর হচ্ছে ১০০ (২০০-এর মধ্যে)।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardMetricCard(
                    title = "মোট মকবোর্ড",
                    value = "${convertBnNum(totalBoards)} বার",
                    subText = "বিগত ৩0 দিনে",
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "গড় ভাইভা নম্বর",
                    value = "${convertBnNum(averageScore)}",
                    subText = "পাস মার্ক: ১০০",
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "সর্বোচ্চ স্কোর",
                    value = "${convertBnNum(maxScore)}",
                    subText = "মোট নম্বর: ২০০",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (historyList.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "এখনো কোনো ভাইভা সেশন সম্পন্ন হয়নি",
                    description = "হোম স্ক্রিনে গিয়ে মক ভাইভা বোর্ডে অংশগ্রহণ করুন। আপনার প্রাপ্ত ফলাফল স্বয়ংক্রিয়ভাবে এখানে যুক্ত হবে।"
                )
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = " ভাইভা প্রগতি লাইন চার্ট (রিয়েল-টাইম)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ট্যাপ করে স্কোর জানুন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Render our advanced interactive area gradient chart!
                        InteractiveAreaChart(
                            items = historyList,
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "মক ভাইভা স্কোরবোর্ড সূচি",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }

            items(historyList.reversed()) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (item.score >= 100) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (item.score >= 100) "🎉" else "⚠️",
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "বিসিএস মক ভাইভা সেশন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "তারিখ: ${item.dateLabel}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${convertBnNum(item.score)} / ২০০",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = if (item.score >= 100) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Text(
                                text = if (item.score >= 100) "সফল (পাস)" else "অনুত্তীর্ণ",
                                fontSize = 11.sp,
                                color = if (item.score >= 100) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Area Chart with touch tooltip mapping
@OptIn(ExperimentalTextApi::class)
@Composable
fun InteractiveAreaChart(
    items: List<VivaHistoryItem>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var selectedIdx by remember { mutableStateOf<Int?>(null) }
    var touchX by remember { mutableFloatStateOf(-1f) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .pointerInput(items) {
                detectTapGestures(
                    onPress = { offset ->
                        val width = size.width
                        val paddingLeft = 40.dp.toPx()
                        val paddingRight = 10.dp.toPx()
                        val chartWidth = width - paddingLeft - paddingRight
                        val stepX = chartWidth / (items.size - 1).coerceAtLeast(1)

                        val relativeX = offset.x - paddingLeft
                        val idx = (relativeX / stepX + 0.5f).toInt().coerceIn(0, items.size - 1)
                        selectedIdx = idx
                        touchX = paddingLeft + (idx * stepX)
                    }
                )
            }
            .pointerInput(items) {
                detectDragGestures(
                    onDragEnd = { selectedIdx = null },
                    onDragCancel = { selectedIdx = null },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val width = size.width
                        val paddingLeft = 40.dp.toPx()
                        val paddingRight = 10.dp.toPx()
                        val chartWidth = width - paddingLeft - paddingRight
                        val stepX = chartWidth / (items.size - 1).coerceAtLeast(1)

                        val relativeX = change.position.x - paddingLeft
                        val idx = (relativeX / stepX + 0.5f).toInt().coerceIn(0, items.size - 1)
                        selectedIdx = idx
                        touchX = paddingLeft + (idx * stepX)
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height

        val paddingLeft = 40.dp.toPx()
        val paddingRight = 10.dp.toPx()
        val paddingTop = 10.dp.toPx()
        val paddingBottom = 30.dp.toPx()

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Draw horizontal grid lines (0, 50, 100, 150, 200)
        val horizontalLevels = listOf(0, 50, 100, 150, 200)
        horizontalLevels.forEach { level ->
            val fraction = level.toFloat() / 200f
            val y = height - paddingBottom - (fraction * chartHeight)

            // Grid Line
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Label text drawing
            drawText(
                textMeasurer = textMeasurer,
                text = convertBnNum(level),
                topLeft = Offset(5.dp.toPx(), y - 8.dp.toPx()),
                style = TextStyle(fontSize = 10.sp, color = labelColor)
            )
        }

        // Draw points and curves
        if (items.isNotEmpty()) {
            val points = mutableListOf<Offset>()
            val stepX = chartWidth / (items.size - 1).coerceAtLeast(1)

            items.forEachIndexed { index, item ->
                val x = paddingLeft + (index * stepX)
                val fractionY = item.score.toFloat() / 200f
                val y = height - paddingBottom - (fractionY * chartHeight)
                points.add(Offset(x, y))
            }

            // Build path for Area filling
            val fillPath = Path()
            fillPath.moveTo(paddingLeft, height - paddingBottom)

            points.forEachIndexed { idx, point ->
                if (idx == 0) {
                    fillPath.lineTo(point.x, point.y)
                } else {
                    // Draw cubic bezier curve or linear
                    val prevPoint = points[idx - 1]
                    fillPath.cubicTo(
                        (prevPoint.x + point.x) / 2, prevPoint.y,
                        (prevPoint.x + point.x) / 2, point.y,
                        point.x, point.y
                    )
                }
            }
            fillPath.lineTo(points.last().x, height - paddingBottom)
            fillPath.close()

            // Draw area gradient background
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                    startY = paddingTop,
                    endY = height - paddingBottom
                )
            )

            // Build path for Stroke Line
            val strokePath = Path()
            points.forEachIndexed { idx, point ->
                if (idx == 0) {
                    strokePath.moveTo(point.x, point.y)
                } else {
                    val prevPoint = points[idx - 1]
                    strokePath.cubicTo(
                        (prevPoint.x + point.x) / 2, prevPoint.y,
                        (prevPoint.x + point.x) / 2, point.y,
                        point.x, point.y
                    )
                }
            }

            drawPath(
                path = strokePath,
                color = primaryColor,
                style = Stroke(width = 3.2.dp.toPx())
            )

            // Draw dots at points
            points.forEachIndexed { index, point ->
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = point
                )
            }

            // Draw x axis labels for dates
            val labelCount = if (items.size > 5) 5 else items.size
            val labelSpacing = (items.size - 1).coerceAtLeast(1) / (labelCount - 1).coerceAtLeast(1)
            for (i in 0 until labelCount) {
                val idx = (i * labelSpacing).coerceIn(0, items.size - 1)
                val item = items[idx]
                val point = points[idx]
                val textLayoutResult = textMeasurer.measure(
                    text = item.dateLabel,
                    style = TextStyle(fontSize = 10.sp, color = labelColor, fontWeight = FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(point.x - (textLayoutResult.size.width / 2), height - 20.dp.toPx())
                )
            }

            // Draw Tooltip selection if tapped
            selectedIdx?.let { idx ->
                val activeItem = items[idx]
                val activePoint = points[idx]

                // Vertical indicator line
                drawLine(
                    color = primaryColor.copy(alpha = 0.8f),
                    start = Offset(activePoint.x, paddingTop),
                    end = Offset(activePoint.x, height - paddingBottom),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )

                // Outer pulsing circle on intersection
                drawCircle(
                    color = primaryColor.copy(alpha = 0.25f),
                    radius = 12.dp.toPx(),
                    center = activePoint
                )
                drawCircle(
                    color = primaryColor,
                    radius = 6.dp.toPx(),
                    center = activePoint
                )

                // Tooltip box content drawing
                val tooltipString = "তারিখ: ${activeItem.dateLabel}\nস্কোর: ${convertBnNum(activeItem.score)}"
                val textLayoutResult = textMeasurer.measure(
                    text = tooltipString,
                    style = TextStyle(fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                )

                val tooltipWidth = textLayoutResult.size.width + 16.dp.toPx()
                val tooltipHeight = textLayoutResult.size.height + 12.dp.toPx()

                var tooltipX = activePoint.x - (tooltipWidth / 2)
                tooltipX = tooltipX.coerceIn(10.dp.toPx(), width - tooltipWidth - 10.dp.toPx())

                val tooltipY = (activePoint.y - tooltipHeight - 12.dp.toPx()).coerceAtLeast(10.dp.toPx())

                // Draw rounded background
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(tooltipX, tooltipY),
                    size = Size(tooltipWidth, tooltipHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(tooltipX + 8.dp.toPx(), tooltipY + 6.dp.toPx())
                )
            }
        }
    }
}

// ==========================================
// TAB 2: MCQ QUIZ PERFORMANCE (rounded vertical bar chart with click tooltips)
// ==========================================
@Composable
fun QuizAnalyticsTab(
    historyList: List<QuizHistoryItem>,
    bookmarkedQuestions: List<com.example.data.models.QuestionEntity> = emptyList(),
    onToggleBookmark: (String) -> Unit = {}
) {
    val totalSolvedMcqs = historyList.sumOf { it.total }
    val totalCorrectMcqs = historyList.sumOf { it.correct }
    val averageAccuracy = if (totalSolvedMcqs == 0) 0 else ((totalCorrectMcqs.toFloat() / totalSolvedMcqs.toFloat()) * 100).toInt()
    val activeDaysCount = historyList.size

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "অনুশীলন কুইজ পারফরম্যান্স ট্র্যাকিং",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "প্রতিদিনের ড্যাশবোর্ড কুইজ কিংবা অধ্যায়ভিত্তিক কুইজের পারফরম্যান্স। নিখুঁতভাবে আপনার একিউরেসি ও ভলিউম বৃদ্ধি পর্যবেক্ষণ করুন।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardMetricCard(
                    title = "মোট একিউরেসি",
                    value = "${convertBnNum(averageAccuracy)}%",
                    subText = "টার্গেট: ৮০%+",
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "মোট সমাধানকৃত",
                    value = "${convertBnNum(totalCorrectMcqs)} টি",
                    subText = "উত্তর প্রদান: ${convertBnNum(totalSolvedMcqs)}",
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "কুইজ অনুশীলিত দিন",
                    value = "${convertBnNum(activeDaysCount)} দিন",
                    subText = "৩০ দিনের মধ্যে",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (historyList.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "এখনো কুইজ বা প্র্যাকটিস সম্পূর্ণ হয়নি",
                    description = "হোম পেজের 'আজকের প্র্যাকটিস' কিংবা 'মিনি মক টেস্ট' দিয়ে আপনার প্রথম কুইজ শেষ করুন। কুইজ শেষ করলেই আপনার সামগ্রিক একিউরেসি এখানে আপডেটেড হবে।"
                )
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "কুইজ একিউরেসি বার চার্ট (বিগত ১২ দিন)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ট্যাপ করুন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Show last 12 quiz days to prevent display cluttering and ensure high readability
                        val lastQuizDays = historyList.takeLast(12)
                        QuizBarChart(
                            items = lastQuizDays,
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "টিপস",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "স্মার্ট কুইজ টিপস: আপনার ৫০% কমে থাকা বিষয়গুলোতে এআই কোচ বেশি গুরুত্ব দেবে। আপনি নিয়মিত অনুশীলনের মাধ্যমে এখানে একিউরেসি ক্রমান্বয়ে বৃদ্ধি দেখতে পাবেন।",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF14532D)
                        )
                    }
                }
            }

            if (bookmarkedQuestions.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "🔖 বুকমার্কড জটিল প্রশ্নসমূহ (${convertBnNum(bookmarkedQuestions.size)}টি)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(bookmarkedQuestions) { question ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("bookmarked_question_${question.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (question.subjectId == "math") "গণিত" else if (question.subjectId == "bangla") "বাংলা" else if (question.subjectId == "english") "ইংরেজি" else "সাধারণ জ্ঞান",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                IconButton(onClick = { onToggleBookmark(question.id) }) {
                                    Icon(
                                        imageVector = BookmarkIcon,
                                        contentDescription = "বুকমার্ক মুছুন",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = question.question,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Show options
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "ক) ${question.optionA}", fontSize = 13.sp, color = if (question.correctOption == "A") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (question.correctOption == "A") FontWeight.Bold else FontWeight.Normal)
                                Text(text = "খ) ${question.optionB}", fontSize = 13.sp, color = if (question.correctOption == "B") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (question.correctOption == "B") FontWeight.Bold else FontWeight.Normal)
                                Text(text = "গ) ${question.optionC}", fontSize = 13.sp, color = if (question.correctOption == "C") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (question.correctOption == "C") FontWeight.Bold else FontWeight.Normal)
                                Text(text = "ঘ) ${question.optionD}", fontSize = 13.sp, color = if (question.correctOption == "D") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (question.correctOption == "D") FontWeight.Bold else FontWeight.Normal)
                            }
                            
                            if (question.explanation.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "ব্যাখ্যা: ${question.explanation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom interactive vertical Bar Chart mapping quiz accuracies
@OptIn(ExperimentalTextApi::class)
@Composable
fun QuizBarChart(
    items: List<QuizHistoryItem>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var selectedIdx by remember { mutableStateOf<Int?>(null) }

    val secondaryColor = MaterialTheme.colorScheme.secondary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier.pointerInput(items) {
            detectTapGestures { offset ->
                val width = size.width
                val paddingLeft = 40.dp.toPx()
                val paddingRight = 10.dp.toPx()
                val chartWidth = width - paddingLeft - paddingRight

                val barBlockWidth = chartWidth / items.size.coerceAtLeast(1)
                val relativeX = offset.x - paddingLeft
                val idx = (relativeX / barBlockWidth).toInt().coerceIn(0, items.size - 1)
                selectedIdx = if (selectedIdx == idx) null else idx
            }
        }
    ) {
        val width = size.width
        val height = size.height

        val paddingLeft = 40.dp.toPx()
        val paddingRight = 10.dp.toPx()
        val paddingTop = 15.dp.toPx()
        val paddingBottom = 25.dp.toPx()

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Draw horizontal levels (0%, 25%, 50%, 75%, 100%)
        val levels = listOf(0, 25, 50, 75, 100)
        levels.forEach { level ->
            val fraction = level.toFloat() / 100f
            val y = height - paddingBottom - (fraction * chartHeight)

            drawLine(
                color = Color.LightGray.copy(alpha = 0.4f),
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1.dp.toPx()
            )

            drawText(
                textMeasurer = textMeasurer,
                text = "${convertBnNum(level)}%",
                topLeft = Offset(5.dp.toPx(), y - 8.dp.toPx()),
                style = TextStyle(fontSize = 10.sp, color = labelColor)
            )
        }

        if (items.isNotEmpty()) {
            val barBlockWidth = chartWidth / items.size
            val barWidth = barBlockWidth * 0.55f // spacing between bars

            items.forEachIndexed { index, item ->
                val fractionY = item.accuracy / 100f
                val barHeight = fractionY * chartHeight

                val blockStartX = paddingLeft + (index * barBlockWidth)
                val barStartX = blockStartX + (barBlockWidth - barWidth) / 2
                val barStartY = height - paddingBottom - barHeight

                // Draw solid bar with rounded corners on top
                drawRoundRect(
                    color = if (selectedIdx == index) secondaryColor else secondaryColor.copy(alpha = 0.65f),
                    topLeft = Offset(barStartX, barStartY),
                    size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Date label underneath
                if (index % 2 == 0 || items.size <= 6) {
                    val textLayoutResult = textMeasurer.measure(
                        text = item.dateLabel,
                        style = TextStyle(fontSize = 9.sp, color = labelColor, fontWeight = FontWeight.Bold)
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(blockStartX + (barBlockWidth - textLayoutResult.size.width) / 2, height - 18.dp.toPx())
                    )
                }

                // Tooltip drawn above target bar
                if (selectedIdx == index) {
                    val tooltipString = "সঠিক: ${convertBnNum(item.correct)}/${convertBnNum(item.total)}\nএকিউরেসি: ${convertBnNum(item.accuracy.toInt())}%"
                    val textLayoutResult = textMeasurer.measure(
                        text = tooltipString,
                        style = TextStyle(fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    )

                    val tooltipWidth = textLayoutResult.size.width + 12.dp.toPx()
                    val tooltipHeight = textLayoutResult.size.height + 10.dp.toPx()

                    var tooltipX = barStartX + (barWidth / 2) - (tooltipWidth / 2)
                    tooltipX = tooltipX.coerceIn(10.dp.toPx(), width - tooltipWidth - 10.dp.toPx())

                    val tooltipY = (barStartY - tooltipHeight - 6.dp.toPx()).coerceAtLeast(2.dp.toPx())

                    drawRoundRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(tooltipX, tooltipY),
                        size = Size(tooltipWidth, tooltipHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(tooltipX + 6.dp.toPx(), tooltipY + 5.dp.toPx())
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 3: STUDY PLANNER COMPLETION RATES (GITHUB CONTRIBUTION GRID MATRIX)
// ==========================================
@Composable
fun PlannerAnalyticsTab(historyList: List<PlannerHistoryItem>) {
    val totalDays = historyList.size
    val averageRate = if (historyList.isEmpty()) 0 else historyList.map { it.rate }.average().toInt()
    val highDays = historyList.count { it.rate >= 80 }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "রুটিন ও অধ্যায় শেষ করার প্রগতি",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "আপনার জেনারেট করা রুটিন টাস্কগুলোর দৈনিক সমাপ্ত হওয়ার হার (০% - ১০০%)। বেশি বিষয় সম্পন্ন করলে সমাপ্তির হার বৃদ্ধি পাবে।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardMetricCard(
                    title = "গড় সমাপ্তির হার",
                    value = "${convertBnNum(averageRate)}%",
                    subText = "টার্গেট: ৭০%+",
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "উচ্চ প্রগতির দিন",
                    value = "${convertBnNum(highDays)} দিন",
                    subText = "৮০%+ সমাপ্তির সাথে",
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "পর্যবেক্ষিত দিন",
                    value = "${convertBnNum(totalDays)} দিন",
                    subText = "বিগত ৩0 দিনে",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (historyList.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "রুটিন সংক্রান্ত কোনো তথ্য নেই",
                    description = "রুটিন স্ক্রিনে গিয়ে অধ্যায়ন সম্পন্ন হিসেবে চিহ্নিত করুন। আপনার সমাপ্তির হার স্বয়ংক্রিয়ভাবে প্রতিদিন হিসেব হবে।"
                )
            }
        } else {
            // Circle Radial Gauge Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.0f).padding(end = 12.dp)) {
                            Text(
                                text = "আজকের রুটিন সম্পূর্ণতার মিটার",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "আপনার প্রতিদিনের রুটিনে অন্তর্ভুক্ত টপিক বা অধ্যায়গুলো সম্পন্ন করার উপর ভিত্তি করে এই গতিমাপকটি সাজানো হয়েছে।",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }

                        // Circular Radial Dial Component
                        RadialCompletionDial(
                            progressPercent = averageRate / 100f,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            // GitHub Contributions Grid Mock Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "৩০ দিনের রুটিন সমাপ্তির ট্র্যাক গ্রিড",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "রুটিনের গ্যামিফায়েড গ্রিড ভিউ (রঙের কালচে ভাব দৈনিক প্রগতির মাত্রা নির্দেশ করে)।",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )

                        // 30 Days Contributions styled layout
                        PlannerContribGrid(historyList)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Grid Legend indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("কম ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            listOf(
                                Color(0xFFE2E8F0), 
                                Color(0xFF93C5FD), 
                                Color(0xFF3B82F6), 
                                Color(0xFF1D4ED8)
                            ).forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(color)
                                )
                            }
                            Text(" বেশি", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// GitHub Styled Contributions Grid View
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlannerContribGrid(items: List<PlannerHistoryItem>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val blockColor = when {
                item.rate == 0 -> Color(0xFFE2E8F0) // slate 200: unattempted
                item.rate < 40 -> Color(0xFF93C5FD) // light blue
                item.rate < 75 -> Color(0xFF3B82F6) // general active blue
                else -> Color(0xFF1D4ED8) // dark sapphire blue for complete masterly rate
            }

            var showTooltip by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(blockColor)
                    .clickable { showTooltip = !showTooltip }
            ) {
                if (showTooltip) {
                    AlertDialog(
                        onDismissRequest = { showTooltip = false },
                        title = null,
                        text = {
                            Text(
                                text = "তারিখ: ${item.dateLabel}\nরুটিন সম্পূর্ণতার হার: ${convertBnNum(item.rate)}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { showTooltip = false }) {
                                Text("ওকে")
                            }
                        }
                    )
                }
            }
        }
    }
}

// Circular Speed Meter / Gauge Canvas Radial completion Dial
@Composable
fun RadialCompletionDial(
    progressPercent: Float,
    modifier: Modifier = Modifier
) {
    val strokeWidth = 8.dp
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizeMin = size.minDimension
            val radius = (sizeMin - strokeWidth.toPx()) / 2

            // Background circular gray track
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.35f),
                radius = radius,
                style = Stroke(width = strokeWidth.toPx())
            )

            // Foreground completed path
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * progressPercent,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx())
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${convertBnNum((progressPercent * 100).toInt())}%",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = primaryColor
            )
            Text(
                text = "গড় প্রগতি",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Visual layout helper classes
@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    subText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subText,
                fontSize = 9.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EmptyStateCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📈", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

// Convert numbers of integer to Bangla text dynamically
private fun convertBnNum(number: Int): String {
    val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val str = number.toString()
    val builder = StringBuilder()
    for (element in str) {
        if (element in '0'..'9') {
            builder.append(bnDigits[element - '0'])
        } else {
            builder.append(element)
        }
    }
    return builder.toString()
}
