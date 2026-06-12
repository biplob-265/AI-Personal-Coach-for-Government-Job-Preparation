package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.VivaConfig
import com.example.ui.viewmodel.VivaQuestion
import com.example.ui.viewmodel.VivaReport
import com.example.ui.viewmodel.VivaStage
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BcsVivaSimulatorScreen(
    vivaStage: VivaStage,
    vivaConfig: VivaConfig,
    vivaQuestions: List<VivaQuestion>,
    currentIndex: Int,
    isLoading: Boolean,
    vivaReport: VivaReport?,
    onUpdateConfig: (String, String, String, String) -> Unit,
    onStartViva: () -> Unit,
    onSubmitResponse: (String) -> Unit,
    onRestartViva: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var autoSpeakEnabled by remember { mutableStateOf(true) }

    // TTS Setup & Lifecycle management
    DisposableEffect(context) {
        val speechObj = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        try {
            speechObj.language = Locale("bn", "BD")
        } catch (e: Exception) {
            speechObj.language = Locale.getDefault()
        }
        tts = speechObj

        onDispose {
            speechObj.stop()
            speechObj.shutdown()
        }
    }

    // Function to read text out loud
    val speakSpeech = { textToSpeak: String ->
        if (isTtsReady && tts != null) {
            // strip speaker part for cleaner speech
            val speechContent = textToSpeak.substringAfter(":")
            tts?.speak(speechContent, TextToSpeech.QUEUE_FLUSH, null, "BCS_VIVA_SPEECH_ID")
        }
    }

    // Auto voice output on question changes
    LaunchedEffect(vivaQuestions.size, currentIndex, vivaStage) {
        if (vivaStage == VivaStage.INTERVIEWING && vivaQuestions.isNotEmpty() && currentIndex < vivaQuestions.size) {
            val curQuestion = vivaQuestions[currentIndex]
            if (autoSpeakEnabled) {
                // Wait briefly for UI transitions, then speak
                kotlinx.coroutines.delay(600)
                speakSpeech("${curQuestion.speaker}: ${curQuestion.questionText}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "বিসিএস এআই ভাইভা বোর্ড",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("viva_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "পিছনে যান")
                    }
                },
                actions = {
                    if (vivaStage == VivaStage.INTERVIEWING) {
                        IconButton(
                            onClick = { autoSpeakEnabled = !autoSpeakEnabled },
                            modifier = Modifier.testTag("toggle_autospeak")
                        ) {
                            Text(
                                text = if (autoSpeakEnabled) "🔊" else "🔇",
                                fontSize = 18.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            when (vivaStage) {
                VivaStage.SETUP -> {
                    BcsVivaSetupView(
                        config = vivaConfig,
                        onUpdateConfig = onUpdateConfig,
                        onStartViva = onStartViva
                    )
                }
                VivaStage.INTERVIEWING -> {
                    BcsInterviewingView(
                        questions = vivaQuestions,
                        currentIndex = currentIndex,
                        isLoading = isLoading,
                        onSpeak = { text -> speakSpeech(text) },
                        onSubmitAnswer = onSubmitResponse,
                        totalQuestions = vivaConfig.totalQuestions
                    )
                }
                VivaStage.EVALUATING -> {
                    BcsEvaluatingView()
                }
                VivaStage.COMPLETED -> {
                    BcsCompletedView(
                        report = vivaReport,
                        onRestart = onRestartViva
                    )
                }
            }
        }
    }
}

// SETUP VIEW
@Composable
fun BcsVivaSetupView(
    config: VivaConfig,
    onUpdateConfig: (String, String, String, String) -> Unit,
    onStartViva: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Temporary view-only fields to let user edit in text form or selection
    var districtInput by remember { mutableStateOf(config.homeDistrict) }
    var subjectInput by remember { mutableStateOf(config.academicSubject) }

    // Dropdown lists
    val cadreList = listOf(
        "BCS Administration (প্রশাসন)",
        "BCS Police (পুলিশ ক্যাডার)",
        "BCS Foreign Affairs (পররাষ্ট্র ক্যাডার)",
        "BCS Taxation (কর ক্যাডার)",
        "BCS Education (শিক্ষা ক্যাডার)",
        "BCS Custom & Excise (কাস্টমস)"
    )
    val moodList = listOf(
        "Friendly (সহৃদয় ও মার্জিত)",
        "Strict / Stress Interview (কঠোর ও মানসিক চাপযুক্ত)",
        "Detailed / Academic (গভীর তথ্য ও একাডেমিক অনুসন্ধানকারী)"
    )

    var selectedCadre by remember { mutableStateOf(config.cadrePreference) }
    var selectedMood by remember { mutableStateOf(config.boardTemperament) }

    var isCadreExpanded by remember { mutableStateOf(false) }
    var isMoodExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "মক ভাইভা",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(60.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                .padding(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "বিসিএস ভাইভা কো-অর্ডিনেটর",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "আপনার নিজের প্রোফাইল সাজিয়ে মক ভাইভা সেশন শুরু করুন। জেমিনী এআই রিয়েল ভাইভা বোর্ডের ৩ জন সদস্যের স্বকীয় অনুভূতিতে ভাইভা পরিচালনা করবে।",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Preference Selectors Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ভাইভা বোর্ড কনফিগারেশন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // First choice cadre dropdown
                Text(
                    text = "প্রথম চয়েস ক্যাডার (First Choice Cadre)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { isCadreExpanded = true }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedCadre, fontSize = 14.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown")
                    }
                    DropdownMenu(
                        expanded = isCadreExpanded,
                        onDismissRequest = { isCadreExpanded = false }
                    ) {
                        cadreList.forEach { cadre ->
                            DropdownMenuItem(
                                text = { Text(cadre) },
                                onClick = {
                                    selectedCadre = cadre
                                    isCadreExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Academic Honors Subject Text input
                OutlinedTextField(
                    value = subjectInput,
                    onValueChange = { subjectInput = it },
                    label = { Text("অনার্স/স্নাতক বিষয় (Graduation Major)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_input"),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = "subject") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // District text input
                OutlinedTextField(
                    value = districtInput,
                    onValueChange = { districtInput = it },
                    label = { Text("নিজ জেলা (Home District)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("district_input"),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = "district") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Board Temperament style dropdown
                Text(
                    text = "ভাইভা বোর্ডের মনোভাব (Board Temperament)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { isMoodExpanded = true }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedMood, fontSize = 14.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown")
                    }
                    DropdownMenu(
                        expanded = isMoodExpanded,
                        onDismissRequest = { isMoodExpanded = false }
                    ) {
                        moodList.forEach { mood ->
                            DropdownMenuItem(
                                text = { Text(mood) },
                                onClick = {
                                    selectedMood = mood
                                    isMoodExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start button
        Button(
            onClick = {
                onUpdateConfig(selectedCadre, subjectInput, districtInput, selectedMood)
                onStartViva()
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(50.dp)
                .testTag("start_viva_simulation_btn"),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "start")
            Spacer(modifier = Modifier.width(8.dp))
            Text("ভাইভা বোর্ড কক্ষে প্রবেশ করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// INTERVIEW SCREEN VIEW (MOCK MULTIPLE BOARD SECTOR AND VOICE INPUTS)
@Composable
fun BcsInterviewingView(
    questions: List<VivaQuestion>,
    currentIndex: Int,
    isLoading: Boolean,
    onSpeak: (String) -> Unit,
    onSubmitAnswer: (String) -> Unit,
    totalQuestions: Int
) {
    val currentQuestion = questions.getOrNull(currentIndex)
    var userAnswerText by remember { mutableStateOf("") }

    // Trigger input text reset on question index changes
    LaunchedEffect(currentIndex) {
        userAnswerText = ""
    }

    // Voice recognition launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.firstOrNull() ?: ""
            if (spokenText.isNotEmpty()) {
                userAnswerText = spokenText
            }
        }
    }

    val runVoiceSTT = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
            putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("bn-BD", "en-US"))
            putExtra(RecognizerIntent.EXTRA_PROMPT, "আপনার মুখে উত্তরটি পরিষ্কারভাবে বলুন...")
        }
        try {
            voiceLauncher.launch(intent)
        } catch (e: Exception) {
            // Speech recognizer missing handler
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progression Header
        val displayIndex = (currentIndex + 1).coerceAtMost(totalQuestions)
        val progressPercent = displayIndex.toFloat() / totalQuestions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "বোর্ড প্রশ্ন: ${convertVivaBnNum(displayIndex)} / ${convertVivaBnNum(totalQuestions)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "প্রগতি: ${convertVivaBnNum((progressPercent * 100).toInt())}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { progressPercent },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Boards visualizer scene!
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2E3B4E),
                            Color(0xFF1B2330)
                        )
                    )
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Board Member 1 (Expert 1)
                BoardMemberVisualizer(
                    title = "বিশেষজ্ঞ সদস্য ১",
                    iconColor = Color(0xFFFFB74D),
                    isActive = currentQuestion?.speaker?.contains("সদস্য ১") == true
                )

                // Board Member 2 (Chairman)
                BoardMemberVisualizer(
                    title = "চেয়ারম্যান",
                    iconColor = Color(0xFF4FC3F7),
                    isActive = currentQuestion?.speaker?.contains("চেয়ারম্যান") == true
                )

                // Board Member 3 (Expert 2)
                BoardMemberVisualizer(
                    title = "বিশেষজ্ঞ সদস্য ২",
                    iconColor = Color(0xFF81C784),
                    isActive = currentQuestion?.speaker?.contains("সদস্য ২") == true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current Board Question bubble
        if (currentQuestion != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎤 ${currentQuestion.speaker} বলছেন:",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 14.sp
                        )

                        // Speaker button to play voice
                        IconButton(
                            onClick = { onSpeak("${currentQuestion.speaker}: ${currentQuestion.questionText}") },
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                        ) {
                            Text(
                                text = "🔊",
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentQuestion.questionText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interacting User voice recorder interface!
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "আপনার উত্তর দিন",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // The giant interactive pulsing mic button in center
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable { runVoiceSTT() }
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing ring effect via standard infinite animation
                    val infiniteTransition = rememberInfiniteTransition(label = "mic_ring")
                    val ringScale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.35f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearOutSlowInEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ring_scale"
                    )
                    val ringAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 0.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearOutSlowInEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ring_alpha"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(ringScale)
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = ringAlpha), CircleShape)
                    )

                    Text(
                        text = "🎙️",
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "কথা বলতে মাইক বাটনে ট্যাপ করুন",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Editable text box in case user needs to modify voice or type
                OutlinedTextField(
                    value = userAnswerText,
                    onValueChange = { userAnswerText = it },
                    placeholder = { Text("কথা বলুন অথবা এখানে আপনার উত্তর লিখুন...", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("viva_answer_text"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit button
        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("ভাইভা বোর্ড উত্তর পর্যালোচনা করছে...", fontSize = 13.sp)
        } else {
            Button(
                onClick = { onSubmitAnswer(userAnswerText) },
                enabled = userAnswerText.trim().isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
                    .testTag("submit_viva_answer_btn"),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("উত্তর সাবমিট করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Send, contentDescription = "submit")
            }
        }
    }
}

@Composable
fun BoardMemberVisualizer(
    title: String,
    iconColor: Color,
    isActive: Boolean
) {
    val scale = if (isActive) 1.15f else 1.0f
    val borderWidth = if (isActive) 2.dp else 1.dp
    val borderColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
    val speakerBg = if (isActive) iconColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .width(85.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(speakerBg, CircleShape)
                .border(borderWidth, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "member",
                tint = if (isActive) iconColor else Color.Gray,
                modifier = Modifier.size(28.dp)
            )

            if (isActive) {
                // Active Speaking green light badge
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.Green, CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isActive) Color.White else Color.LightGray,
            textAlign = TextAlign.Center
        )
    }
}

// EVALUATION PING VIEW
@Composable
fun BcsEvaluatingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant loading ripple animation
            val infiniteTransition = rememberInfiniteTransition(label = "ripple")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "star",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ভিডিও ও উত্তর মূল্যায়ন করা হচ্ছে...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "বিসিএস ক্যাডার বোর্ড মেম্বাররা আপনার দেওয়া ভাইভার উচ্চারণ, শিষ্টাচার এবং জ্ঞানের গভীরতা কাস্টম জেমিনী মডেলে মূল্যায়ন করে রিপোর্ট তৈরি করছেন।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// FULL EVALUATION REPORT CARD SCREEN VIEW
@Composable
fun BcsCompletedView(
    report: VivaReport?,
    onRestart: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (report != null) {
            // Pass Fail Badge Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (report.passStatus) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ),
                border = BorderStroke(1.dp, if (report.passStatus) Color(0xFFC8E6C9) else Color(0xFFFFCDD2))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (report.passStatus) "🎉 ভাইভায় উত্তীর্ণ হয়েছেন!" else "🎯 আরও অনুশীলন দরকার!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (report.passStatus) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                        Text(
                            text = "মোট ভাইভা নম্বর: ${convertVivaBnNum(200)}-এর মধ্যে",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }

                    // Score overlay
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                if (report.passStatus) Color(0xFF2E7D32) else Color(0xFFC62828),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = convertVivaBnNum(report.overallScore),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Criteria Analysis Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "বিশদ কর্মক্ষমতা মিটার (Performance)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. Content depth
                    FeedbackProgressMetric(
                        title = "জ্ঞানের গভীরতা ও তথ্যের সঠিকতা",
                        percentage = 0.85f,
                        feedback = report.feedbackOnContent
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Decorum
                    FeedbackProgressMetric(
                        title = "শিষ্টাচার, মনোভাব ও আত্মবিশ্বাস",
                        percentage = 0.90f,
                        feedback = report.feedbackOnPoliteness
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Spoken articulation
                    FeedbackProgressMetric(
                        title = "বাচনভঙ্গি ও স্পষ্টতা",
                        percentage = 0.78f,
                        feedback = report.feedbackOnFluency
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Board overall reviews
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "বোর্ড মেম্বারদের মন্তব্য (Board Review)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Chairman overall statement
                    BoardMemberCommentRow(
                        memberTitle = "চেয়ারম্যান (Chairman Overall)",
                        comment = report.chairmanRemarks,
                        badgeColor = Color(0xFF4FC3F7)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Expert 1 and 2
                    BoardMemberCommentRow(
                        memberTitle = "বিশেষজ্ঞ সদস্য ১ (ক্যাডার ও সাবজেক্ট)",
                        comment = report.expert1Remarks,
                        badgeColor = Color(0xFFFFB74D)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    BoardMemberCommentRow(
                        memberTitle = "বিশেষজ্ঞ সদস্য ২ (পরিস্থিতি ও সাধারণ জ্ঞান)",
                        comment = report.expert2Remarks,
                        badgeColor = Color(0xFF81C784)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Strategic Roadmap suggestions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "info",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "আপনার জন্য জেমিনী এআই গাইডলাইট:",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = report.evaluationSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "পরবর্তী ভাইভা প্রস্তুতির রোডম্যাপ:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = report.preparationRoadmap,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Restart Mock Interview Button
        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(48.dp)
                .testTag("restart_viva_simulation_btn"),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "restart")
            Spacer(modifier = Modifier.width(8.dp))
            Text("নতুন মক ভাইভা সেশন শুরু করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Progress metrics helper visual block
@Composable
fun FeedbackProgressMetric(
    title: String,
    percentage: Float,
    feedback: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(convertVivaBnNum((percentage * 100).toInt()) + "%", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = feedback,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
    }
}

// Comments from member helpers
@Composable
fun BoardMemberCommentRow(
    memberTitle: String,
    comment: String,
    badgeColor: Color
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(badgeColor, CircleShape)
                .align(Alignment.Top)
                .padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = memberTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = comment,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}

// Helpers for string conversions of numbers in screen file
private fun convertVivaBnNum(number: Int): String {
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
