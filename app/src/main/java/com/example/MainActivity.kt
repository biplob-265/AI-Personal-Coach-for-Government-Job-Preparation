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

    var activeTabIdx by remember { mutableIntStateOf(0) }

    val isPremium = user?.isPremium == true

    // If an active quiz session or score summary is running, isolate screen layout to prevent distraction
    if (quizState != QuizState.Idle) {
        QuizScreen(
            quizState = quizState,
            onSubmitOption = { model.submitAnswer(it) },
            onNextQuestion = { model.nextQuestion() },
            onExitQuiz = { model.exitQuiz() }
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
                        onStartQuiz = { model.startQuizSession(it) },
                        onResetProgress = { model.resetUserProgress() }
                    )
                    1 -> StudyPlannerScreen(
                        isPremium = isPremium,
                        studyPlanText = planText,
                        isLoading = isPlanLoading,
                        onGeneratePlan = { model.generateStudyPlan() },
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
