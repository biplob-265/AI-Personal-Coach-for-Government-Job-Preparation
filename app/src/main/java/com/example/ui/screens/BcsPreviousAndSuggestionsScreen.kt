package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.BcsQuestionsData
import com.example.data.models.PastBcsQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BcsPreviousAndSuggestionsScreen(
    isPremium: Boolean,
    dynamicSuggestions: String?,
    isSuggestionsLoading: Boolean,
    onGenerateSuggestions: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = বিগত প্রশ্ন ব্যাংক, 1 = এআই সাজেশন ও ভবিষ্যৎবাণী
    val bcsYears = listOf("৪৫তম বিসিএস", "৪৪তম বিসিএস", "৪৩তম বিসিএস", "৪২তম বিসিএস", "৪১তম বিসিএস", "৪০তম বিসিএস")
    var selectedYearFilter by remember { mutableStateOf("৪৫তম বিসিএস") }
    
    // Quiz/Practice sub-mode within previous questions
    var isPracticeMode by remember { mutableStateOf(false) }
    var currentPracticeIndex by remember { mutableStateOf(0) }
    var practiceScore by remember { mutableStateOf(0) }
    var userPracticeAnswers by remember { mutableStateOf<Map<String, String>>(emptyMap()) } // questionId -> selectedOption
    var practiceHasSubmitted by remember { mutableStateOf(false) }
    var selectedPracticeOption by remember { mutableStateOf<String?>(null) }
    var showPracticeResultSummary by remember { mutableStateOf(false) }

    // Grouping questions filtered by year
    val filteredQuestions = remember(selectedYearFilter) {
        BcsQuestionsData.pastQuestions.filter { it.bcsExName.startsWith(selectedYearFilter) }
    }

    // Expanded explanation trackers in reading mode
    var expandedExplanations by remember { mutableStateOf<Set<String>>(emptySet()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "বিগত বিসিএস প্রশ্ন ও এআই সাজেশন",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("bcs_assistant_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "প্রস্থান",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Header Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { 
                        selectedTab = 0 
                        // Reset quiz state when switching tabs
                        isPracticeMode = false
                        showPracticeResultSummary = false
                    },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("বিগত প্রশ্ন ব্যাংক", fontSize = 13.sp, fontWeight = FontWeight.Bold) 
                        }
                    },
                    modifier = Modifier.testTag("bcs_tab_past")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1 
                        // Fire a suggested generation if empty and not loading
                        if (dynamicSuggestions == null && !isSuggestionsLoading) {
                            onGenerateSuggestions()
                        }
                    },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("এআই সাজেশন", fontSize = 13.sp, fontWeight = FontWeight.Bold) 
                        }
                    },
                    modifier = Modifier.testTag("bcs_tab_suggestions")
                )
            }

            if (selectedTab == 0) {
                // TAB 0: PAST QUESTIONS BANK
                Column(modifier = Modifier.fillMaxSize()) {
                    // Filter Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বছর নির্বাচন:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Box(modifier = Modifier.weight(1f)) {
                            var dropdownExpanded by remember { mutableStateOf(false) }
                            Button(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth().height(36.dp).testTag("bcs_year_dropdown"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedYearFilter, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                bcsYears.forEach { year ->
                                    DropdownMenuItem(
                                        text = { Text(year, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            selectedYearFilter = year
                                            dropdownExpanded = false
                                            // Reset quiz state when year changes
                                            isPracticeMode = false
                                            showPracticeResultSummary = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Mode Toggle: Reading Mode vs Instant Mock Test
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { 
                                isPracticeMode = false 
                                showPracticeResultSummary = false
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (!isPracticeMode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (!isPracticeMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("প্রশ্ন পড়ুন (উত্তরসহ)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { 
                                isPracticeMode = true
                                currentPracticeIndex = 0
                                practiceScore = 0
                                userPracticeAnswers = emptyMap()
                                practiceHasSubmitted = false
                                selectedPracticeOption = null
                                showPracticeResultSummary = false
                            },
                            modifier = Modifier.weight(1f).height(38.dp).testTag("start_bcs_test_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPracticeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isPracticeMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("মক টেস্ট দিন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(modifier = Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    if (!isPracticeMode) {
                        // READING MODE SELECTOR
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$selectedYearFilter প্রিলিমিনারি প্রশ্ন ও ব্যাখ্যা",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${filteredQuestions.size} টি প্রশ্ন বিদ্যমান",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (filteredQuestions.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("এই বছরটির প্রশ্নপত্র শীঘ্রই আপডেট হতে যাচ্ছে। অন্য নির্বাচন করুন।", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                }
                            } else {
                                filteredQuestions.forEachIndexed { idx, question ->
                                    val isExpanded = expandedExplanations.contains(question.id)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .testTag("past_question_card_${question.id}"),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "প্রশ্ন - ${idx + 1}",
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(
                                                    text = question.subject,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Text(
                                                text = question.question,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 22.sp
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            // Options block in list row format
                                            ReadingOptionRow(prefix = "A", text = question.optionA, isCorrect = question.correctOption == "A")
                                            ReadingOptionRow(prefix = "B", text = question.optionB, isCorrect = question.correctOption == "B")
                                            ReadingOptionRow(prefix = "C", text = question.optionC, isCorrect = question.correctOption == "C")
                                            ReadingOptionRow(prefix = "D", text = question.optionD, isCorrect = question.correctOption == "D")

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Expandable Explanation Trigger
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                                    .clickable {
                                                        expandedExplanations = if (isExpanded) {
                                                            expandedExplanations - question.id
                                                        } else {
                                                            expandedExplanations + question.id
                                                        }
                                                    }
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "বিসিএস ব্যাখ্যা ও সমাধান",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            if (isExpanded) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                                        .padding(12.dp)
                                                ) {
                                                    Text(
                                                        text = question.explanation,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        lineHeight = 20.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // INTERACTIVE PREVIOUS MOCK TEST MODE
                        if (showPracticeResultSummary) {
                            // TEST END SCORE SUMMARY CARD
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "$selectedYearFilter সাব কুইজ সম্পন্ন!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("আপনার অর্জন স্কোর", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("${practiceScore} / ${filteredQuestions.size}", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        val efficiency = (practiceScore.toDouble() / filteredQuestions.size) * 100.0
                                        Text(
                                            text = if (efficiency >= 80) "দুর্দান্ত প্রস্তুতি! আপনি পাস মার্ক অতি সহজেই ছুয়ে ফেলবেন।" else if (efficiency >= 50) "সন্তোষজনক প্রস্তুতি, দুর্বল টপিকগুলোর ব্যাখ্যা ভালো করে রিভিশন দিন।" else "আরেকটু পড়াশোনা করা দরকার। স্পেশাল এআই সাজেশনগুলো দেখুন।",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 20.sp,
                                            color = if (efficiency >= 80) Color(0xFF1B5E20) else Color(0xFFE65100),
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // List of answers summary
                                Text(
                                    text = "প্রশ্ন রিভিউ ও সঠিক উত্তরমালা:",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )

                                filteredQuestions.forEachIndexed { qIdx, q ->
                                    val userAns = userPracticeAnswers[q.id] ?: ""
                                    val isCorrect = userAns == q.correctOption

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                            .border(1.dp, if (isCorrect) Color(0xFF81C784) else Color(0xFFE57373), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text("প্রশ্ন ${qIdx + 1}: ${q.question}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("আপনার পছন্দ: ${if (userAns.isNotEmpty()) userAns else "অগ্রাহ্য"} (${if (isCorrect) "সঠিক" else "ভুল"})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828))
                                        Text("সঠিক উত্তর: ${q.correctOption}. ${when(q.correctOption) {
                                            "A" -> q.optionA
                                            "B" -> q.optionB
                                            "C" -> q.optionC
                                            "D" -> q.optionD
                                            else -> ""
                                        }}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("ব্যাখ্যা: ${q.explanation}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { 
                                        isPracticeMode = false
                                        showPracticeResultSummary = false
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("প্রশ্ন পড়তে ফিরে যান", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // ACTIVE SINGLE MCQ TEST PAGE
                            if (filteredQuestions.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("এই বছরের কোনো প্রশ্ন আপাতত নেই।", fontSize = 14.sp)
                                }
                            } else {
                                val scrollState = rememberScrollState()
                                val currentQ = filteredQuestions[currentPracticeIndex]

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$selectedYearFilter মক টেস্ট",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${currentPracticeIndex + 1} / ${filteredQuestions.size}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { (currentPracticeIndex.toFloat() + 1) / filteredQuestions.size },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Question Box Card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(currentQ.subject, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(currentQ.question, fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // 4 MCQ rows interactive
                                    PracticeOptionInteractiveRow(prefix = "A", text = currentQ.optionA, isSelected = selectedPracticeOption == "A", hasSubmitted = practiceHasSubmitted, isCorrect = currentQ.correctOption == "A") {
                                        if (!practiceHasSubmitted) {
                                            selectedPracticeOption = "A"
                                            practiceHasSubmitted = true
                                            userPracticeAnswers = userPracticeAnswers + (currentQ.id to "A")
                                            if (currentQ.correctOption == "A") practiceScore += 1
                                        }
                                    }
                                    PracticeOptionInteractiveRow(prefix = "B", text = currentQ.optionB, isSelected = selectedPracticeOption == "B", hasSubmitted = practiceHasSubmitted, isCorrect = currentQ.correctOption == "B") {
                                        if (!practiceHasSubmitted) {
                                            selectedPracticeOption = "B"
                                            practiceHasSubmitted = true
                                            userPracticeAnswers = userPracticeAnswers + (currentQ.id to "B")
                                            if (currentQ.correctOption == "B") practiceScore += 1
                                        }
                                    }
                                    PracticeOptionInteractiveRow(prefix = "C", text = currentQ.optionC, isSelected = selectedPracticeOption == "C", hasSubmitted = practiceHasSubmitted, isCorrect = currentQ.correctOption == "C") {
                                        if (!practiceHasSubmitted) {
                                            selectedPracticeOption = "C"
                                            practiceHasSubmitted = true
                                            userPracticeAnswers = userPracticeAnswers + (currentQ.id to "C")
                                            if (currentQ.correctOption == "C") practiceScore += 1
                                        }
                                    }
                                    PracticeOptionInteractiveRow(prefix = "D", text = currentQ.optionD, isSelected = selectedPracticeOption == "D", hasSubmitted = practiceHasSubmitted, isCorrect = currentQ.correctOption == "D") {
                                        if (!practiceHasSubmitted) {
                                            selectedPracticeOption = "D"
                                            practiceHasSubmitted = true
                                            userPracticeAnswers = userPracticeAnswers + (currentQ.id to "D")
                                            if (currentQ.correctOption == "D") practiceScore += 1
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    AnimatedVisibility(
                                        visible = practiceHasSubmitted,
                                        enter = fadeIn()
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (selectedPracticeOption == currentQ.correctOption) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                                            ),
                                            border = BorderStroke(1.dp, if (selectedPracticeOption == currentQ.correctOption) Color(0xFFBBF7D0) else Color(0xFFFECACA))
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    text = if (selectedPracticeOption == currentQ.correctOption) "🎉 অভিনন্দন! সঠিক উত্তর।" else "❌ ভুল উত্তর হয়েছে!",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (selectedPracticeOption == currentQ.correctOption) Color(0xFF15803D) else Color(0xFFB91C1C)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "সঠিক উত্তর: ${currentQ.correctOption}. ${when(currentQ.correctOption) {
                                                        "A" -> currentQ.optionA
                                                        "B" -> currentQ.optionB
                                                        "C" -> currentQ.optionC
                                                        "D" -> currentQ.optionD
                                                        else -> ""
                                                    }}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF15803D)
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "ব্যাখ্যা: ${currentQ.explanation}",
                                                    fontSize = 12.sp,
                                                    lineHeight = 18.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    Button(
                                        onClick = {
                                            if (currentPracticeIndex + 1 < filteredQuestions.size) {
                                                currentPracticeIndex += 1
                                                selectedPracticeOption = null
                                                practiceHasSubmitted = false
                                            } else {
                                                showPracticeResultSummary = true
                                            }
                                        },
                                        enabled = practiceHasSubmitted,
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("next_bcs_question_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = if (currentPracticeIndex + 1 < filteredQuestions.size) "পরবর্তী প্রশ্ন" else "ফলাফল দেখুন",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // TAB 1: AI SUGGESTION & FUTURE PREDICTION SCREEN
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Title card
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💡", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "বিজ্ঞানসম্মত ভবিষ্যৎবাণী ও এআই সাজেশন",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "বিগত ১৫ টি বিসিএস পরীক্ষার ট্রেন্ড বিশ্লেষণ এবং আপনার চ্যাপ্টার ভিত্তিক অনুশীলনের একিউরেসি স্কোর সমন্বয় করে এই টার্গেট সাজেশনগুলো তৈরি করা হয়েছে।",
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Static Preloaded Subjects Predictions
                    Text(
                        text = "বিষয়ভিত্তিক হাই-প্রোবাবিলিটি সাজেশন:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    BcsQuestionsData.preloadedPredictions.forEach { pred ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(pred.subject, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(pred.topic, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFFEBEE), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(pred.probability, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("💡 ট্রিপ্স: ${pred.tip}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text("নমুনা অনুমানভিত্তিক প্রশ্ন:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text(pred.sampleQuestion, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 4.dp))
                                
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    pred.sampleOptions.forEachIndexed { sIdx, opt ->
                                        val optPrefix = when(sIdx) { 0 -> "A"; 1 -> "B"; 2 -> "C"; else -> "D" }
                                        Text(
                                            text = "• $optPrefix. $opt",
                                            fontSize = 12.sp,
                                            fontWeight = if (optPrefix == pred.correctOption) FontWeight.Bold else FontWeight.Normal,
                                            color = if (optPrefix == pred.correctOption) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("সঠিক উত্তর ব্যাখ্যা: ${pred.sampleExplanation}", fontSize = 11.sp, color = Color(0xFF2E7D32), lineHeight = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // AI Gemini Suggestions Box Creator
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2F6)),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🤖", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("এআই মেন্টর জেনারেটেড ফাইনাল সাজেশন", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "আপনার রিভিশন স্পিড বাড়াতে এবং আপকামিং ৪৬তম বিসিএস অনুযায়ী আপনার পারফরম্যান্স ডেটা বিশ্লেষণ করে সম্পূর্ণ ডায়নামিক এআই সাজেশন তৈরি করুন।",
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isSuggestionsLoading) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("এআই ট্রেন্ড অ্যান্ড স্ট্যাটিসটিক্স জেনারেট হচ্ছে...", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            } else if (dynamicSuggestions != null) {
                                // RENDER DETAILED AI GEMINI MARKDOWN SUGGESTION
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(10.dp))
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = dynamicSuggestions,
                                        fontSize = 13.sp,
                                        lineHeight = 22.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onGenerateSuggestions,
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("পূনরায় সাজেশন আপডেট করুন", fontSize = 13.sp)
                                }
                            } else {
                                Button(
                                    onClick = onGenerateSuggestions,
                                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("generate_bcs_suggestions_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("এআই ব্যক্তিগত সাজেশন তৈরি করুন", fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ReadingOptionRow(
    prefix: String,
    text: String,
    isCorrect: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                if (isCorrect) Color(0xFFE8F5E9) else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .padding(vertical = 6.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(
                    if (isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(11.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = prefix,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCorrect) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Medium,
            color = if (isCorrect) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PracticeOptionInteractiveRow(
    prefix: String,
    text: String,
    isSelected: Boolean,
    hasSubmitted: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val bgCol = when {
        hasSubmitted && isCorrect -> Color(0xFFE8F5E9)
        hasSubmitted && isSelected && !isCorrect -> Color(0xFFFFEBEE)
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderCol = when {
        hasSubmitted && isCorrect -> Color(0xFF2E7D32)
        hasSubmitted && isSelected && !isCorrect -> Color(0xFFC62828)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(onClick = onClick)
            .testTag("practice_opt_${prefix}_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgCol),
        border = BorderStroke(1.2.dp, borderCol)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = prefix,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
