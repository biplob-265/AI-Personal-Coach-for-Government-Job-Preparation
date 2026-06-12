package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.StudyPlannerScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.QuizState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val model: MainViewModel = viewModel()

    val user by model.user.collectAsState()
    val topicStats by model.topicStatsList.collectAsState()
    val dailyMissionText by model.dailyMission.collectAsState()
    val isMissionLoading by model.isMissionLoading.collectAsState()
    val planText by model.studyPlan.collectAsState()
    val isPlanLoading by model.isPlanLoading.collectAsState()
    val chatMessages by model.chatMessages.collectAsState()
    val isChatLoading by model.isChatLoading.collectAsState()
    val quizState by model.quizState.collectAsState()
    val isProcessingPremium by model.isProcessingPremium.collectAsState()
    val purchaseSuccessMessage by model.purchaseSuccessEvent.collectAsState()

    val dynamicBcsSuggestions by model.dynamicBcsSuggestions.collectAsState()
    val isSuggestionsLoading by model.isSuggestionsLoading.collectAsState()
    val isViewingBcsAssistant by model.isViewingBcsAssistant.collectAsState()

    val isViewingBcsViva by model.isViewingBcsViva.collectAsState()
    val vivaStage by model.vivaStage.collectAsState()
    val vivaConfig by model.vivaConfig.collectAsState()
    val vivaQuestions by model.vivaQuestions.collectAsState()
    val vivaCurrentIndex by model.vivaCurrentIndex.collectAsState()
    val isVivaLoading by model.isVivaLoading.collectAsState()
    val vivaReport by model.vivaReport.collectAsState()

    val isViewingBcsPerformanceDashboard by model.isViewingBcsPerformanceDashboard.collectAsState()
    val isViewingBcsFlashcards by model.isViewingBcsFlashcards.collectAsState()
    val flashcardsList by model.flashcards.collectAsState()
    val lastActiveQuiz by model.lastActiveQuizState.collectAsState()
    val vivaHistoryList by model.vivaHistoryList.collectAsState()
    val quizHistoryList by model.quizHistoryList.collectAsState()
    val plannerHistoryList by model.plannerHistoryList.collectAsState()

    val targetExamName by model.targetExamName.collectAsState()
    val targetExamDate by model.targetExamDate.collectAsState()
    val dailyStudyHours by model.dailyStudyHours.collectAsState()
    val completedTopicIds by model.completedTopicIds.collectAsState()
    val bookmarkedQuestionIds by model.bookmarkedQuestionIds.collectAsState()
    val bookmarkedQuestions by model.bookmarkedQuestions.collectAsState()

    var activeTabIdx by remember { mutableIntStateOf(0) }

    val isPremium = user?.isPremium == true

    // If an active quiz session or score summary is running, isolate screen layout to prevent distraction
    if (quizState != QuizState.Idle) {
        QuizScreen(
            quizState = quizState,
            onSubmitOption = { model.submitAnswer(it) },
            onNextQuestion = { model.nextQuestion() },
            onExitQuiz = { model.exitQuiz() },
            onToggleBookmark = { model.toggleBookmark(it) },
            bookmarkedQuestionIds = bookmarkedQuestionIds
        )
    } else if (isViewingBcsAssistant) {
        com.example.ui.screens.BcsPreviousAndSuggestionsScreen(
            isPremium = isPremium,
            dynamicSuggestions = dynamicBcsSuggestions,
            isSuggestionsLoading = isSuggestionsLoading,
            onGenerateSuggestions = { model.generateDynamicBcsSuggestions() },
            onBack = { model.setViewingBcsAssistant(false) }
        )
    } else if (isViewingBcsViva) {
        com.example.ui.screens.BcsVivaSimulatorScreen(
            vivaStage = vivaStage,
            vivaConfig = vivaConfig,
            vivaQuestions = vivaQuestions,
            currentIndex = vivaCurrentIndex,
            isLoading = isVivaLoading,
            vivaReport = vivaReport,
            onUpdateConfig = { cadre, subject, district, temperament ->
                model.updateVivaConfig(cadre, subject, district, temperament)
            },
            onStartViva = { model.startVivaSession() },
            onSubmitResponse = { model.submitVivaAnswer(it) },
            onRestartViva = { model.restartViva() },
            onBack = { model.setViewingBcsViva(false) }
        )
    } else if (isViewingBcsPerformanceDashboard) {
        com.example.ui.screens.BcsPerformanceDashboardScreen(
            vivaHistory = vivaHistoryList,
            quizHistory = quizHistoryList,
            plannerHistory = plannerHistoryList,
            bookmarkedQuestions = bookmarkedQuestions,
            onToggleBookmark = { model.toggleBookmark(it) },
            onBack = { model.setViewingBcsPerformanceDashboard(false) }
        )
    } else if (isViewingBcsFlashcards) {
        com.example.ui.screens.BcsFlashcardsScreen(
            flashcards = flashcardsList,
            onAddFlashcard = { front, back, subj -> model.addFlashcard(front, back, subj) },
            onDeleteFlashcard = { model.deleteFlashcard(it) },
            onReviewFlashcard = { card, rating -> model.reviewFlashcard(card, rating) },
            onBack = { model.setViewingBcsFlashcards(false) }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("app_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = activeTabIdx == 0,
                        onClick = { activeTabIdx = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "ড্যাশবোর্ড") },
                        label = { Text("হোম", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_home")
                    )
                    NavigationBarItem(
                        selected = activeTabIdx == 1,
                        onClick = { activeTabIdx = 1 },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "AI প্ল্যানার") },
                        label = { Text("রুটিন", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_planner")
                    )
                    NavigationBarItem(
                        selected = activeTabIdx == 2,
                        onClick = { activeTabIdx = 2 },
                        icon = { Icon(Icons.Default.Face, contentDescription = "AI মেন্টর") },
                        label = { Text("মেন্টর", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_mentor")
                    )
                    NavigationBarItem(
                        selected = activeTabIdx == 3,
                        onClick = { activeTabIdx = 3 },
                        icon = { Icon(Icons.Default.Star, contentDescription = "প্রিমিয়াম") },
                        label = { Text("সাবস্ক্রাইব", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_premium")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTabIdx) {
                    0 -> DashboardScreen(
                        user = user,
                        topicStats = topicStats,
                        dailyMissionText = dailyMissionText,
                        isMissionLoading = isMissionLoading,
                        onGenerateMission = { model.generateDailyMission() },
                        onStartQuiz = { isMock, count -> model.startQuizSession(isMock, null, count) },
                        lastActiveQuiz = lastActiveQuiz,
                        onResumeLastQuiz = { model.resumeLastQuiz() },
                        onResetProgress = { model.resetUserProgress() },
                        onOpenBcsAssistant = { model.setViewingBcsAssistant(true) },
                        onOpenBcsViva = { model.setViewingBcsViva(true) },
                        onOpenPerformanceDashboard = { model.setViewingBcsPerformanceDashboard(true) },
                        onOpenFlashcards = { model.setViewingBcsFlashcards(true) }
                    )
                    1 -> StudyPlannerScreen(
                        isPremium = isPremium,
                        targetExamName = targetExamName,
                        targetExamDate = targetExamDate,
                        dailyStudyHours = dailyStudyHours,
                        completedTopicIds = completedTopicIds,
                        studyPlanText = planText,
                        isLoading = isPlanLoading,
                        onSaveTargetConfig = { name, date, hrs -> model.saveTargetExamConfig(name, date, hrs) },
                        onToggleTopicCompleted = { model.toggleStudyTopicCompleted(it) },
                        onGeneratePlan = { model.generateStudyPlan() },
                        onStartQuizForTopic = { model.startQuizSession(false, it) },
                        onNavigateToPremium = { activeTabIdx = 3 }
                    )
                    2 -> ChatScreen(
                        chatMessages = chatMessages,
                        isLoading = isChatLoading,
                        onSendMessage = { model.sendChatMessage(it) }
                    )
                    3 -> SubscriptionScreen(
                        isPremium = isPremium,
                        isProcessing = isProcessingPremium,
                        successMessage = purchaseSuccessMessage,
                        onPurchasePremium = { model.enrollPremiumSimulated(it) },
                        onClearSuccess = { model.clearPurchaseEvent() }
                    )
                }
            }
        }
    }
}
