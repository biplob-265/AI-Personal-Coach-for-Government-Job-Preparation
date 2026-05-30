package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.QuestionEntity
import com.example.ui.viewmodel.QuizState

@Composable
fun QuizScreen(
    quizState: QuizState,
    onSubmitOption: (String) -> Unit,
    onNextQuestion: () -> Unit,
    onExitQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        when (quizState) {
            is QuizState.Idle -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "প্রস্তুতি শুরু",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "কোন কুইজ সেশন সক্রিয় নেই।",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ড্যাশবোর্ড থেকে একটি টপিক প্র্যাকটিস অথবা মক টেস্ট শুরু করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is QuizState.Active -> {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Top
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BCS প্রাকটিস সেশন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedButton(
                            onClick = onExitQuiz,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red),
                            modifier = Modifier.testTag("exit_quiz_button").height(36.dp)
                        ) {
                            Text("প্রস্থান", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress indicator
                    val total = quizState.questions.size
                    val currentIdx = quizState.currentIndex
                    val progress = (currentIdx.toFloat() + 1) / total

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "প্রশ্ন: ${currentIdx + 1} / $total",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "স্কোর: ${quizState.score}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val question = quizState.questions[currentIdx]

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
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
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = question.question,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // MCQ Options
                    OptionRow(
                        prefix = "A",
                        text = question.optionA,
                        isSelected = quizState.selectedOption == "A",
                        isCorrect = "A" == question.correctOption,
                        hasSubmitted = quizState.hasSubmitted,
                        onClick = { if (!quizState.hasSubmitted) onSubmitOption("A") }
                    )
                    OptionRow(
                        prefix = "B",
                        text = question.optionB,
                        isSelected = quizState.selectedOption == "B",
                        isCorrect = "B" == question.correctOption,
                        hasSubmitted = quizState.hasSubmitted,
                        onClick = { if (!quizState.hasSubmitted) onSubmitOption("B") }
                    )
                    OptionRow(
                        prefix = "C",
                        text = question.optionC,
                        isSelected = quizState.selectedOption == "C",
                        isCorrect = "C" == question.correctOption,
                        hasSubmitted = quizState.hasSubmitted,
                        onClick = { if (!quizState.hasSubmitted) onSubmitOption("C") }
                    )
                    OptionRow(
                        prefix = "D",
                        text = question.optionD,
                        isSelected = quizState.selectedOption == "D",
                        isCorrect = "D" == question.correctOption,
                        hasSubmitted = quizState.hasSubmitted,
                        onClick = { if (!quizState.hasSubmitted) onSubmitOption("D") }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedVisibility(
                        visible = quizState.hasSubmitted,
                        enter = fadeIn() + scaleIn()
                    ) {
                        Column {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        val isCorrect = quizState.selectedOption == question.correctOption
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                            contentDescription = "ফলাফল",
                                            tint = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isCorrect) "সঠিক উত্তর হয়েছে!" else "ভুল উত্তর! সঠিক উত্তর: ${question.correctOption}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                                        )
                                    }
                                    if (question.explanation.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "ব্যাখ্যা: ${question.explanation}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onNextQuestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("next_question_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = if (currentIdx + 1 < total) "পরবর্তী প্রশ্ন" else "কুইজ শেষ করুন",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Next")
                            }
                        }
                    }
                }
            }
            is QuizState.ResultSummary -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "কুইজ সম্পন্ন",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "অভিনন্দন! কুইজ সেশন সম্পন্ন হয়েছে",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "আপনার স্কোর",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${quizState.score} / ${quizState.questions.size}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("সঠিক উত্তর", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${quizState.correctCount} টি",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("ভুল উত্তর", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${quizState.wrongCount} টি",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onExitQuiz,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                            .testTag("back_to_dashboard_button")
                    ) {
                        Text("ড্যাশবোর্ডে ফিরে যান", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OptionRow(
    prefix: String,
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    hasSubmitted: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        hasSubmitted && isCorrect -> Color(0xFFE8F5E9)          // Green highlight for correct answers
        hasSubmitted && isSelected && !isCorrect -> Color(0xFFFFEBEE) // Red highlight for wrong selection
        isSelected -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        hasSubmitted && isCorrect -> Color(0xFF2E7D32)
        hasSubmitted && isSelected && !isCorrect -> Color(0xFFC62828)
        isSelected -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val textWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("option_${prefix}_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = prefix,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = textWeight,
                modifier = Modifier.weight(1.0f)
            )
        }
    }
}
