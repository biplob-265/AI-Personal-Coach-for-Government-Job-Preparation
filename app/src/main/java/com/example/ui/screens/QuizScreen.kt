package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.SolidColor
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

val BookmarkIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Bookmark",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17f, 3f)
            lineTo(7f, 3f)
            curveTo(5.9f, 3f, 5f, 3.9f, 5f, 5f)
            lineTo(5f, 21f)
            lineTo(12f, 18f)
            lineTo(19f, 21f)
            lineTo(19f, 5f)
            curveTo(19f, 3.9f, 18.1f, 3f, 17f, 3f)
            close()
        }
    }.build()

val BookmarkBorderIcon: ImageVector
    get() = ImageVector.Builder(
        name = "BookmarkBorder",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17f, 3f)
            lineTo(7f, 3f)
            curveTo(5.9f, 3f, 5.01f, 3.9f, 5.01f, 5f)
            lineTo(5f, 21f)
            lineTo(12f, 18f)
            lineTo(19f, 21f)
            lineTo(19f, 5f)
            curveTo(19f, 3.9f, 18.1f, 3f, 17f, 3f)
            close()
            moveTo(17f, 18f)
            lineTo(12f, 15.82f)
            lineTo(7f, 18f)
            lineTo(7f, 5f)
            lineTo(17f, 5f)
            lineTo(17f, 18f)
            close()
        }
    }.build()

@Composable
fun QuizScreen(
    quizState: QuizState,
    onSubmitOption: (String) -> Unit,
    onNextQuestion: () -> Unit,
    onExitQuiz: () -> Unit,
    onToggleBookmark: (String) -> Unit = {},
    bookmarkedQuestionIds: Set<String> = emptySet(),
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
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                val isBookmarked = bookmarkedQuestionIds.contains(question.id)
                                IconButton(
                                    onClick = { onToggleBookmark(question.id) },
                                    modifier = Modifier.testTag("bookmark_question_btn_" + question.id)
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) BookmarkIcon else BookmarkBorderIcon,
                                        contentDescription = if (isBookmarked) "বুকমার্ক সরানো" else "বুকমার্ক করুন",
                                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
                                    val isCorrect = quizState.selectedOption == question.correctOption
                                    val correctText = when(question.correctOption) {
                                        "A" -> question.optionA
                                        "B" -> question.optionB
                                        "C" -> question.optionC
                                        "D" -> question.optionD
                                        else -> ""
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                            contentDescription = "ফলাফল",
                                            tint = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isCorrect) "সঠিক উত্তর হয়েছে!" else "ভুল উত্তর হয়েছে!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                                        )
                                    }
                                    if (!isCorrect) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "সঠিক উত্তর: ${question.correctOption}. $correctText",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF2E7D32),
                                            modifier = Modifier.padding(start = 32.dp)
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

                    Spacer(modifier = Modifier.height(28.dp))

                    // SECTION HEADER
                    Text(
                        text = "ভুল উত্তরের ব্যাখ্যা ও সমাধান রিভিউ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val wrongQuestions = quizState.questions.filter { q ->
                        val selected = quizState.userAnswers[q.id]
                        selected != q.correctOption
                    }

                    if (wrongQuestions.isEmpty()) {
                        // All answers correct success card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "সব উত্তর সঠিক হয়েছে!",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "চমৎকার প্রস্তুতি! কুইজের সবকটি প্রশ্নের সঠিক উত্তর দিয়েছেন।",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF166534),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        wrongQuestions.forEachIndexed { index, q ->
                            val userSel = quizState.userAnswers[q.id] ?: ""
                            val userSelText = when (userSel) {
                                "A" -> q.optionA
                                "B" -> q.optionB
                                "C" -> q.optionC
                                "D" -> q.optionD
                                else -> "উত্তর দেওয়া হয়নি"
                            }
                            val corrSelText = when (q.correctOption) {
                                "A" -> q.optionA
                                "B" -> q.optionB
                                "C" -> q.optionC
                                "D" -> q.optionD
                                else -> ""
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFFEE2E2), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "ভুল-${index + 1}",
                                                    color = Color(0xFF991B1B),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (q.subjectId == "math") "গণিত" else if (q.subjectId == "bangla") "বাংলা" else if (q.subjectId == "english") "ইংরেজি" else "সাধারণ জ্ঞান",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = q.question,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 22.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Wrong Selected Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Wrong",
                                            tint = Color(0xFFC62828),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "আপনার উত্তর: ${if (userSel.isNotEmpty()) "$userSel. $userSelText" else "উত্তর দেওয়া হয়নি"}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFC62828)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Correct Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Correct",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "সঠিক উত্তর: ${q.correctOption}. $corrSelText",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }

                                    if (q.explanation.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = "Explanation Icon",
                                                        tint = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "বিশ্লেষণ ও সঠিক ব্যাখ্যা:",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = q.explanation,
                                                    fontSize = 14.sp,
                                                    lineHeight = 22.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
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
