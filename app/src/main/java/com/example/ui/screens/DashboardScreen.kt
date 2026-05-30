package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.UserEntity
import com.example.ui.viewmodel.TopicStats

@Composable
fun DashboardScreen(
    user: UserEntity?,
    topicStats: List<TopicStats>,
    dailyMissionText: String?,
    isMissionLoading: Boolean,
    onGenerateMission: () -> Unit,
    onStartQuiz: (Boolean) -> Unit, // Boolean represents isMockTest
    onResetProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate initial mission on launch if empty
    LaunchedEffect(Unit) {
        if (dailyMissionText == null && !isMissionLoading) {
            onGenerateMission()
        }
    }

    val totalAttempted = topicStats.sumOf { it.totalAnswers }
    val totalCorrect = topicStats.sumOf { it.correctAnswers }
    val overallAccuracy = if (totalAttempted == 0) {
        0.0
    } else {
        (totalCorrect.toDouble() / totalAttempted) * 100.0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        // Welcome Header (High Density layout)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar bubble
                    val nameChar = user?.name?.firstOrNull()?.uppercase() ?: "R"
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nameChar.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Column {
                        Text(
                            text = "বিসিএস এআই কোচ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "টার্গেট: ${user?.targetExam ?: "৪৬তম বিসিএস"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (user?.isPremium == true) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8DEF8), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PREMIUM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quiz Launch Quick Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { onStartQuiz(false) },
                modifier = Modifier
                    .weight(1.0f)
                    .height(44.dp)
                    .testTag("practice_questions_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Practice")
                Spacer(modifier = Modifier.width(4.dp))
                Text("আজকের প্র্যাকটিস", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { onStartQuiz(true) },
                modifier = Modifier
                    .weight(1.0f)
                    .height(44.dp)
                    .testTag("start_mock_test_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Star, contentDescription = "Mock")
                Spacer(modifier = Modifier.width(4.dp))
                Text("মিনি মক টেস্ট", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Performance metrics section
        Text(
            text = "আপনার প্রস্তুতি স্কোরকার্ড",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatsCard(
                title = "মোট উত্তর",
                value = "$totalAttempted টি",
                subText = "সঠিক: $totalCorrect টি",
                modifier = Modifier.weight(1.0f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            StatsCard(
                title = "গড় একিউরেসি",
                value = "${overallAccuracy.toInt()}%",
                subText = if (overallAccuracy < 50 && totalAttempted > 0) "দুর্বল (উন্নতি প্রয়োজন)" else if (totalAttempted == 0) "এখনো শুরু হয়নি" else "ভালো প্রস্তুতি",
                valueColor = if (overallAccuracy >= 75) Color(0xFF2E7D32) else if (overallAccuracy >= 50) Color(0xFFE65100) else Color(0xFFB3261E),
                modifier = Modifier.weight(1.0f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Daily Mission section (Gemini generator card in High Density style)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityAiBg),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, com.example.ui.theme.HighDensityAiBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "এআই মেন্টর সাজেশন",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Mission Refresh Icon
                    IconButton(
                        onClick = onGenerateMission,
                        enabled = !isMissionLoading,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                RoundedCornerShape(16.dp)
                            )
                            .size(36.dp)
                            .testTag("refresh_mission_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "মিশন রিফ্রেশ",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isMissionLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "আপনার জন্য নতুন এজেন্ডা সাজানো হচ্ছে...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else if (dailyMissionText != null) {
                    // Internal high-contrast card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityAiInnerBg),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, com.example.ui.theme.HighDensityAiInnerBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "আজকের মিশন (২ ঘণ্টা সময়)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = dailyMissionText,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Text(
                        text = "কোনো মিশন এজেন্ডা লোড হয়নি। পুনরায় শুরু করতে রিফ্রেশ বাটনে ক্লিক করুন।",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Start Practice Quick Button under AI suggestions
                Button(
                    onClick = { onStartQuiz(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("practice_questions_button_ai"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("মিশন শুরু করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Topic accuracy and dynamic engine warning
        Text(
            text = "অধ্যায় ভিত্তিক একিউরেসি তালিকা",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "স্মার্ট কুইজ সিস্টেম আপনার ৫০% এর কম একিউরেসি প্রাপ্ত সাবজেক্টগুলো বেশি অনুশীলন করাবে।",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Render Topics with 2-column High Density Grid
        topicStats.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { stat ->
                    Box(modifier = Modifier.weight(1f)) {
                        HighDensityTopicCard(stat = stat)
                    }
                }
                // If odd number, pad the right side with empty Box of equal weight
                if (pair.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Reset Settings button
        OutlinedButton(
            onClick = onResetProgress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("reset_stats_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Text("প্রস্তুতি এবং প্র্যাকটিস হিস্ট্রি রিসেট করুন", fontSize = 12.sp)
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    subText: String,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun HighDensityTopicCard(stat: TopicStats) {
    val accuracy = stat.accuracyPercentage
    val (statusLabel, badgeColor) = when (stat.difficultyStatus) {
        "STRONG" -> "সন্তোষজনক" to Color(0xFF21005D)
        "MEDIUM" -> "মাঝারি" to Color(0xFF6750A4)
        "WEAK" -> "দুর্বল" to Color(0xFFB3261E)
        else -> "কোনো রেকর্ড নেই" to Color(0xFF757575)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("topic_stat_row_${stat.topic.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stat.topic.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = if (accuracy >= 0) "${accuracy.toInt()}%" else "- %",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = if (accuracy >= 0) badgeColor else Color(0xFF757575)
                )

                if (accuracy >= 0) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (accuracy / 100.0).coerceIn(0.0, 1.0).toFloat())
                                .height(4.dp)
                                .background(badgeColor)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = statusLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
        }
    }
}

// Minimal placeholder IconButton wrapper for easy styling
@Composable
fun IconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
