package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale

@Composable
fun StudyPlannerScreen(
    isPremium: Boolean,
    targetExamName: String,
    targetExamDate: String,
    dailyStudyHours: Int,
    completedTopicIds: Set<String>,
    studyPlanText: String?,
    isLoading: Boolean,
    onSaveTargetConfig: (String, String, Int) -> Unit,
    onToggleTopicCompleted: (String) -> Unit,
    onGeneratePlan: () -> Unit,
    onStartQuizForTopic: (String) -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = রুটিন (Routine Checklist), 1 = এআই বিশ্লেষণ (AI Breakdown)
    var isEditingSettings by remember { mutableStateOf(false) }

    // Input States
    var tempExamName by remember { mutableStateOf(targetExamName) }
    var tempExamDate by remember { mutableStateOf(targetExamDate) }
    var tempHours by remember { mutableIntStateOf(dailyStudyHours) }

    // Synchronize inputs whenever state changes
    LaunchedEffect(targetExamName, targetExamDate, dailyStudyHours) {
        tempExamName = targetExamName
        tempExamDate = targetExamDate
        tempHours = dailyStudyHours
    }

    val daysRemaining = getDaysRemaining(targetExamDate)
    val studyTasks = getStudyDays(targetExamDate, dailyStudyHours)

    // Calculate progress
    val totalTasks = studyTasks.size
    val completedCount = studyTasks.count { completedTopicIds.contains(it.id) }
    val progressPercent = if (totalTasks > 0) (completedCount.toFloat() / totalTasks) else 0.0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App bar top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BCS স্টাডি শিডিউলার",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "সিলেবাস ভিত্তিক দৈনিক পড়ার প্ল্যান",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { isEditingSettings = !isEditingSettings },
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("toggle_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "শিডিউল পরিবর্তন",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Target Date Countdown and Progress Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "তারিখ",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = targetExamName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        val formattedTargetDate = try {
                            val sdfInput = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val sdfOutput = java.text.SimpleDateFormat("dd MMMM, yyyy", Locale("bn"))
                            val date = sdfInput.parse(targetExamDate)
                            if (date != null) sdfOutput.format(date) else targetExamDate
                        } catch (e: Exception) {
                            targetExamDate
                        }
                        Text(
                            text = "পরীক্ষার তারিখ: $formattedTargetDate",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Days Remaining Countdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = if (daysRemaining > 0) "পরীক্ষার বাকি আর" else "পরীক্ষার দিন আজ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = if (daysRemaining > 0) "${convertToBanglaNumber(daysRemaining)} দিন!" else "উৎসাহ নিয়ে দিন শুরু করুন!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "দৈনিক পড়া: ${convertToBanglaNumber(dailyStudyHours)} ঘণ্টা",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Indicator
                val progressPercentText = (progressPercent * 100).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "লার্নিং প্রগ্রেস: $completedCount/${totalTasks} শেষ হয়েছে",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${convertToBanglaNumber(progressPercentText)}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .testTag("planner_progress_bar"),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
            }
        }

        // Collapsible Target Editor Segment
        AnimatedVisibility(
            visible = isEditingSettings,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "শিডিউল কনফিগারেশন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = tempExamName,
                        onValueChange = { tempExamName = it },
                        label = { Text("পরীক্ষার নাম") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_exam_name"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tempExamDate,
                        onValueChange = { tempExamDate = it },
                        label = { Text("পরীক্ষার তারিখ (YYYY-MM-DD)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_exam_date"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Study Hours allocation
                    Text(
                        text = "দৈনিক পড়া বরাদ্দ: ${convertToBanglaNumber(tempHours)} ঘণ্টা",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(2, 4, 6, 8, 10).forEach { hr ->
                            val isSelected = tempHours == hr
                            Button(
                                onClick = { tempHours = hr },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                                    .height(38.dp)
                                    .testTag("hour_selector_$hr")
                            ) {
                                Text(convertToBanglaNumber(hr), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { isEditingSettings = false },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("বাতিল")
                        }
                        Button(
                            onClick = {
                                onSaveTargetConfig(tempExamName, tempExamDate, tempHours)
                                isEditingSettings = false
                            },
                            modifier = Modifier.testTag("save_settings_button")
                        ) {
                            Text("শিডিউল আপডেট")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sliding Tab Row to Switch between syllabus checklist and deep AI analysis
        TabRow(
            selectedTabIndex = activeTab,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            containerColor = Color.Transparent,
            divider = {}
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("📅 সিলেবাস প্ল্যান", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_syllabus_plan")
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("🤖 এআই মেন্টর বিশ্লেষণ", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_ai_analysis")
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Content Display
        Box(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
        ) {
            if (activeTab == 0) {
                // Syllables Checklist Plan (Tab 0)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    studyTasks.forEach { task ->
                        val isDone = completedTopicIds.contains(task.id)
                        var isExpanded by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .animateContentSize(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isDone) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isDone,
                                        onCheckedChange = { onToggleTopicCompleted(task.id) },
                                        modifier = Modifier.testTag("checkbox_${task.id}")
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { isExpanded = !isExpanded }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Subject bubble
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        getSubjectColor(task.subjectId).copy(alpha = 0.15f),
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = task.subjectName,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = getSubjectColor(task.subjectId)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = task.dayText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = task.topicName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = if (isDone) TextDecoration.LineThrough else null,
                                            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                        )

                                        Text(
                                            text = task.dateString,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(onClick = { isExpanded = !isExpanded }) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Expand details"
                                        )
                                    }
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "সিলেবাস ফোকাস এবং গাইডলাইন:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = task.focusPoints,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "বরাদ্দ সময়: ${convertToBanglaNumber(task.durationHours)} ঘণ্টা",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Button(
                                            onClick = { onStartQuizForTopic(task.topicId) },
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("start_quiz_${task.id}"),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "শুরু",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("প্র্যাকটিস কুইজ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                // AI Custom Mentor Analyzer (Tab 1)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "জেমিনী আপনার কাস্টম রুটিন সাজাচ্ছে...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "তারিখ, দিনের ব্যবধান ও পূর্ববর্তী ফল বিশ্লেষণ করা হচ্ছে।",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else if (studyPlanText != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "জেমিনী এআই জেনারেটেড সাজেশন",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                TextButton(
                                    onClick = onGeneratePlan,
                                    modifier = Modifier.testTag("regenerate_ai_plan_button")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("রিলোড করুন")
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = studyPlanText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 24.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    } else {
                        // Empty/Ready state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            RoundedCornerShape(36.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "তথ্য",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "এআই দিয়ে স্পেশাল রুটিন তৈরি করুন",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "জেমিনী এআই আপনার লক্ষ্য $targetExamName ($daysRemaining দিন বাকি) এবং প্র্যাকটিস পারফর্ম্যান্সের উপর ভিত্তি করে একটি প্রফেশনাল ও সুনির্দিষ্ট পড়াশোনার কৌশল ও দুর্বলতা রুটিন প্রস্তুত করবে।",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = onGeneratePlan,
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(48.dp)
                                        .testTag("generate_initial_plan_button")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "ঝাড়ু")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("এআই রুটিন জেনারেট করুন")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper color resolver
@Composable
fun getSubjectColor(subjectId: String): Color {
    return when (subjectId) {
        "math" -> Color(0xFFE53935)
        "bangla" -> Color(0xFF4CAF50)
        "english" -> Color(0xFF1E88E5)
        else -> Color(0xFFFFB300)
    }
}

fun getDaysRemaining(targetDateStr: String): Long {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val targetDate = sdf.parse(targetDateStr) ?: return 180L
        val currentDate = sdf.parse(sdf.format(java.util.Date())) ?: return 0L
        val diffInMs = targetDate.time - currentDate.time
        if (diffInMs < 0) return 0L
        java.util.concurrent.TimeUnit.DAYS.convert(diffInMs, java.util.concurrent.TimeUnit.MILLISECONDS)
    } catch (e: Exception) {
        180L
    }
}

fun convertToBanglaNumber(number: Long): String {
    val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val str = number.toString()
    val builder = StringBuilder()
    for (char in str) {
        if (char in '0'..'9') {
            builder.append(bnDigits[char - '0'])
        } else {
            builder.append(char)
        }
    }
    return builder.toString()
}

fun convertToBanglaNumber(number: Int): String {
    return convertToBanglaNumber(number.toLong())
}

data class StudyTask(
    val id: String,
    val dayText: String,
    val dateString: String,
    val subjectId: String,
    val subjectName: String,
    val topicId: String,
    val topicName: String,
    val focusPoints: String,
    val durationHours: Int
)

fun getStudyDays(startDateString: String, dailyHours: Int): List<StudyTask> {
    val sdfInput = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfOutput = java.text.SimpleDateFormat("dd MMMM, yyyy", Locale("bn"))
    val startCalendar = Calendar.getInstance()
    try {
        val parsedDate = sdfInput.parse(startDateString)
        if (parsedDate != null) {
            startCalendar.time = parsedDate
            // Subtract syllabus offset to align studying days nicely ending right on target date or rolling weekly
            startCalendar.add(Calendar.DAY_OF_YEAR, -7)
        }
    } catch (e: Exception) {}

    val syllabus = listOf(
        Triple("math", "শতকরা (Percentage)", "percentage" to "শতকরার মৌলিক সূত্রাবলী, বিগত ১০ বছরের বিসিএস প্রশ্নের গাণিতিক সমস্যা সমাধান এবং প্র্যাকটিস কুইজ টেস্ট।"),
        Triple("bangla", "বাংলা সাহিত্য (Literature)", "literature" to "চর্যাপদ ও প্রাচীন যুগ, মধ্যযুগের কবি-মনীষা ও রাজকীয় কাব্যধারা। কবিদের ঐতিহাসিক উক্তি রিভিশন।"),
        Triple("english", "English Grammar: Nouns & Pronouns", "eng_grammar" to "Countability, singular-plural rules, objective-subjective cases and identification of parts of speech."),
        Triple("gk", "বাংলাদেশ বিষয়াবলী (BD Affairs)", "bd_affairs" to "বাংলাদেশের ভৌগোলিক সীমানা, আদিবাসী সংস্কৃতি ও প্রাচীন বাংলার প্রধান রাজবংশসমূহের সংক্ষিপ্ত ইতিহাস।"),
        Triple("math", "লাভ-ক্ষতি (Profit & Loss)", "profit_loss" to "ক্রয়মূল্য, বিক্রয়মূল্য, ছাড় ও লাভের অনুপাত নির্ণয়। লাভ-ক্ষতির শর্টকাট জ্যামিতিক ক্যালকুলেশন ট্রিক্স।"),
        Triple("bangla", "বাংলা ব্যাকরণ ও ধ্বনিতত্ত্ব (Grammar)", "grammar" to "ধ্বনি পরিবর্তন, সন্ধির নিয়মাবলী, সমাসের সহজ গঠন ও প্রয়োগ নিয়ে বাস্তব সমাধান।"),
        Triple("english", "English Grammar: Prepositions & Agreement", "eng_grammar" to "Appropriate prepositions lists, subject-verb agreement exceptions, and correcting sentence errors.")
    )

    return syllabus.mapIndexed { idx, (subjId, topicName, topicIdAndFocus) ->
        val currentCal = startCalendar.clone() as Calendar
        currentCal.add(Calendar.DAY_OF_YEAR, idx)
        
        val dateStr = try {
            sdfOutput.format(currentCal.time)
        } catch (e: Exception) {
            "দিন ${idx + 1}"
        }

        val subjectName = when (subjId) {
            "math" -> "গণিত"
            "bangla" -> "বাংলা"
            "english" -> "ইংরেজি"
            else -> "সাধারণ জ্ঞান"
        }

        StudyTask(
            id = "study_day_${idx + 1}",
            dayText = "দিন ${idx + 1}",
            dateString = dateStr,
            subjectId = subjId,
            subjectName = subjectName,
            topicId = topicIdAndFocus.first,
            topicName = topicName,
            focusPoints = topicIdAndFocus.second,
            durationHours = dailyHours
        )
    }
}
