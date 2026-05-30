package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.QuestionEntity
import com.example.data.models.SubjectEntity
import com.example.data.models.TopicEntity
import com.example.data.models.UserAnswerEntity
import com.example.data.models.UserEntity
import com.example.data.repository.JobCoachRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

// Representation of a Topic with its dynamic accuracy and status
data class TopicStats(
    val topic: TopicEntity,
    val subjectName: String,
    val totalAnswers: Int,
    val correctAnswers: Int,
    val accuracyPercentage: Double, // -1.0 means Not Attempted
    val difficultyStatus: String // "WEAK" (<50%), "MEDIUM" (50-75%), "STRONG" (>=75%), "UNATTEMPTED"
)

// Represents Chat Message
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

// UI Quiz State
sealed interface QuizState {
    object Idle : QuizState
    data class Active(
        val questions: List<QuestionEntity>,
        val currentIndex: Int,
        val selectedOption: String?, // "A", "B", "C", "D"
        val hasSubmitted: Boolean,
        val score: Int
    ) : QuizState
    data class ResultSummary(
        val questions: List<QuestionEntity>,
        val score: Int,
        val correctCount: Int,
        val wrongCount: Int
    ) : QuizState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = JobCoachRepository(application)

    // Data streams from Room
    val user: StateFlow<UserEntity?> = repository.user.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val subjects: StateFlow<List<SubjectEntity>> = repository.subjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val topics: StateFlow<List<TopicEntity>> = repository.topics.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val questions: StateFlow<List<QuestionEntity>> = repository.questions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val answers: StateFlow<List<UserAnswerEntity>> = repository.answers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI state for topic stats list
    private val _topicStatsList = MutableStateFlow<List<TopicStats>>(emptyList())
    val topicStatsList: StateFlow<List<TopicStats>> = _topicStatsList.asStateFlow()

    // AI Mentor Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "AI",
                text = "আসসালামু আলাইকুম! আমি আপনার বিসিএস (BCS) ও সরকারি চাকরি প্রস্তুতির AI মেন্টর। আপনার গণিত, বাংলা বা সাধারণ জ্ঞানের দুর্বলতা নিয়ে আমার সাথে আলোচনা করুন। আমি আপনার বর্তমান কর্মক্ষমতা অনুযায়ী নির্দেশনা দেব।"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Study Plan State
    private val _studyPlan = MutableStateFlow<String?>(null)
    val studyPlan: StateFlow<String?> = _studyPlan.asStateFlow()

    private val _isPlanLoading = MutableStateFlow(false)
    val isPlanLoading: StateFlow<Boolean> = _isPlanLoading.asStateFlow()

    // Daily Mission State
    private val _dailyMission = MutableStateFlow<String?>(null)
    val dailyMission: StateFlow<String?> = _dailyMission.asStateFlow()

    private val _isMissionLoading = MutableStateFlow(false)
    val isMissionLoading: StateFlow<Boolean> = _isMissionLoading.asStateFlow()

    // Quiz Session State
    private val _quizState = MutableStateFlow<QuizState>(QuizState.Idle)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    // Practice Limit Trackers (Free limits)
    private val _todayMcqCount = MutableStateFlow(0)
    val todayMcqCount: StateFlow<Int> = _todayMcqCount.asStateFlow()

    private val _todayAiMessageCount = MutableStateFlow(0)
    val todayAiMessageCount: StateFlow<Int> = _todayAiMessageCount.asStateFlow()

    // Transaction & Billing UI States
    private val _isProcessingPremium = MutableStateFlow(false)
    val isProcessingPremium: StateFlow<Boolean> = _isProcessingPremium.asStateFlow()

    private val _purchaseSuccessEvent = MutableStateFlow<String?>(null)
    val purchaseSuccessEvent: StateFlow<String?> = _purchaseSuccessEvent.asStateFlow()

    init {
        // Run database seeding and calculate stats once seeded
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
            calculateTopicStats()
            // Observe answers to recalculate stats on any answer insertion
            answers.collect {
                calculateTopicStats()
            }
        }
    }

    // Recalculates stats based on user_answers join.
    suspend fun calculateTopicStats() {
        val allTopics = repository.topicDao.getAllTopicsSync()
        val allSubjects = repository.subjectDao.getAllSubjectsSync()

        val stats = allTopics.map { topic ->
            val answersByTopic = repository.userAnswerDao.getAnswersByTopicSync(topic.id)
            val correctCount = answersByTopic.count { it.isCorrect }

            val accuracy = if (answersByTopic.isEmpty()) {
                -1.0
            } else {
                (correctCount.toDouble() / answersByTopic.size) * 100.0
            }

            val status = when {
                accuracy == -1.0 -> "UNATTEMPTED"
                accuracy < 50.0 -> "WEAK"
                accuracy < 75.0 -> "MEDIUM"
                else -> "STRONG"
            }

            val subjName = allSubjects.find { it.id == topic.subjectId }?.name ?: "অন্যান্য"

            TopicStats(
                topic = topic,
                subjectName = subjName,
                totalAnswers = answersByTopic.size,
                correctAnswers = correctCount,
                accuracyPercentage = accuracy,
                difficultyStatus = status
            )
        }
        _topicStatsList.value = stats
    }

    // Upgrade profile to Premium status
    fun enrollPremiumSimulated(channelName: String) {
        viewModelScope.launch {
            _isProcessingPremium.value = true
            kotlinx.coroutines.delay(1800) // Realistic secure processing delay
            repository.userDao.updatePremiumStatus(true)
            _todayMcqCount.value = 0
            _todayAiMessageCount.value = 0
            _isProcessingPremium.value = false
            _purchaseSuccessEvent.value = "অভিনন্দন! $channelName-এর মাধ্যমে সফলভাবে আপনার অ্যাকাউন্টটি Premium-এ উন্নীত করা হয়েছে।"
        }
    }

    fun clearPurchaseEvent() {
        _purchaseSuccessEvent.value = null
    }

    // Reset progress/answers to refresh data
    fun resetUserProgress() {
        viewModelScope.launch {
            repository.userAnswerDao.clearAllAnswers()
            _todayMcqCount.value = 0
            _todayAiMessageCount.value = 0
            _chatMessages.value = listOf(
                ChatMessage(
                    sender = "AI",
                    text = "আপনার পূর্ববর্তী সকল প্র্যাকটিস হিস্ট্রি রিসেট করা হয়েছে। আসুন আবার নতুন করে প্রস্তুতি শুরু করি!"
                )
            )
            _studyPlan.value = null
            _dailyMission.value = null
            calculateTopicStats()
        }
    }

    // Adaptive Quiz Selection Engine!
    // Centered around: "If topic accuracy < 50% show more questions, Else show next topic"
    fun startQuizSession(isMockTest: Boolean) {
        viewModelScope.launch {
            val allQuestions = repository.questionDao.getAllQuestions().firstOrNull() ?: emptyList()
            if (allQuestions.isEmpty()) return@launch

            val isUserPremium = user.value?.isPremium ?: false
            if (!isUserPremium && _todayMcqCount.value >= 20) {
                // Free limit reached
                return@launch
            }

            // Determine question selection based on weak topic accuracy (< 50%)
            val calculatedStats = _topicStatsList.value
            val weakTopicIds = calculatedStats.filter { it.difficultyStatus == "WEAK" }.map { it.topic.id }

            val chosenQuestions = if (isMockTest) {
                // Mock test is balanced across subjects: shuffle and take 8 questions
                allQuestions.shuffled().take(8)
            } else if (weakTopicIds.isNotEmpty()) {
                // Smart Quiz Engine algorithm: focus questions on Weak Topics (<50% accuracy)
                val weakQuestions = allQuestions.filter { it.topicId in weakTopicIds }
                val otherQuestions = allQuestions.filter { it.topicId !in weakTopicIds }
                // Pull mostly weak, fill with others to make a total of 5 questions
                (weakQuestions.shuffled().take(4) + otherQuestions.shuffled().take(1)).shuffled()
            } else {
                // Next Topic strategy: Find topics with unattempted or next order
                val unattempted = calculatedStats.filter { it.difficultyStatus == "UNATTEMPTED" }
                if (unattempted.isNotEmpty()) {
                    val nextTopicContent = allQuestions.filter { it.topicId == unattempted.first().topic.id }
                    if (nextTopicContent.isNotEmpty()) {
                        nextTopicContent.shuffled().take(5)
                    } else {
                        allQuestions.shuffled().take(5)
                    }
                } else {
                    allQuestions.shuffled().take(5)
                }
            }

            if (chosenQuestions.isNotEmpty()) {
                _quizState.value = QuizState.Active(
                    questions = chosenQuestions,
                    currentIndex = 0,
                    selectedOption = null,
                    hasSubmitted = false,
                    score = 0
                )
            }
        }
    }

    fun submitAnswer(selected: String) {
        val current = _quizState.value
        if (current is QuizState.Active) {
            val q = current.questions[current.currentIndex]
            val isCorrect = selected == q.correctOption

            _quizState.value = current.copy(
                selectedOption = selected,
                hasSubmitted = true,
                score = if (isCorrect) current.score + 1 else current.score
            )
        }
    }

    fun nextQuestion() {
        val current = _quizState.value
        if (current is QuizState.Active) {
            val idx = current.currentIndex
            val activeQuestionsCount = current.questions.size

            // Record previous answer locally to update Room
            viewModelScope.launch {
                val q = current.questions[idx]
                val answeredOption = current.selectedOption ?: ""
                val isCorrect = answeredOption == q.correctOption

                val answerEntity = UserAnswerEntity(
                    questionId = q.id,
                    topicId = q.topicId,
                    subjectId = q.subjectId,
                    isCorrect = isCorrect,
                    answeredAt = repository.getCurrentDateString()
                )
                repository.userAnswerDao.insertAnswer(answerEntity)
                _todayMcqCount.value += 1
            }

            if (idx + 1 < activeQuestionsCount) {
                _quizState.value = current.copy(
                    currentIndex = idx + 1,
                    selectedOption = null,
                    hasSubmitted = false
                )
            } else {
                // Completed
                val totalCorr = if (current.selectedOption == current.questions[idx].correctOption) current.score else current.score
                _quizState.value = QuizState.ResultSummary(
                    questions = current.questions,
                    score = totalCorr,
                    correctCount = totalCorr,
                    wrongCount = activeQuestionsCount - totalCorr
                )
            }
        }
    }

    fun exitQuiz() {
        _quizState.value = QuizState.Idle
    }

    // AI Mentor Chat implementation using Gemini
    // Enforces free user limit check: up to 5 messages/day
    fun sendChatMessage(messageText: String) {
        if (messageText.trim().isEmpty()) return

        val isUserPremium = user.value?.isPremium ?: false
        if (!isUserPremium && _todayAiMessageCount.value >= 5) {
            _chatMessages.value = _chatMessages.value + ChatMessage(sender = "USER", text = messageText) + ChatMessage(
                sender = "AI",
                text = "দুঃখিত! ফ্রি অ্যাকাউন্টে আপনার আজকের AI মেসেজ রেঞ্জ লিমিট (৫ টি) শেষ হয়েছে। অতিরিক্ত এআই চ্যাট ও মেন্টর সুবিধা পেতে Premium সাবস্ক্রিপশন চালু করুন।"
            )
            return
        }

        val userMsg = ChatMessage(sender = "USER", text = messageText)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            // Compile student metrics from Room stats
            val currentStats = _topicStatsList.value
            val mathAccuracy = currentStats.filter { it.topic.subjectId == "math" }.map { it.accuracyPercentage }.filter { it >= 0 }.average()
            val banglaAccuracy = currentStats.filter { it.topic.subjectId == "bangla" }.map { it.accuracyPercentage }.filter { it >= 0 }.average()
            val englishAccuracy = currentStats.filter { it.topic.subjectId == "english" }.map { it.accuracyPercentage }.filter { it >= 0 }.average()
            val gkAccuracy = currentStats.filter { it.topic.subjectId == "gk" }.map { it.accuracyPercentage }.filter { it >= 0 }.average()

            val statsPromptPart = """
                Student Performance data:
                - Math Accuracy: ${if (mathAccuracy.isNaN()) "No attempts yet" else "${mathAccuracy.toInt()}%"}
                - Bangla Accuracy: ${if (banglaAccuracy.isNaN()) "No attempts yet" else "${banglaAccuracy.toInt()}%"}
                - English Accuracy: ${if (englishAccuracy.isNaN()) "No attempts yet" else "${englishAccuracy.toInt()}%"}
                - General Knowledge (GK) Accuracy: ${if (gkAccuracy.isNaN()) "No attempts yet" else "${gkAccuracy.toInt()}%"}
            """.trimIndent()

            val prompt = """
                You are a supportive Bangladesh Government Job / BCS Exam Coach.
                $statsPromptPart
                
                The student says: "$messageText"
                
                Please answer in Bangla helpful feedback, specific study advice, and motivational guidance tailored to their current performance level.
            """.trimIndent()

            val aiResponse = repository.generateWithGemini(
                prompt = prompt,
                systemInstruction = "You are an expert BCS mentor. Speak in polite Bangla and motivate the candidate. Keep formatting clean with lists where helpful."
            )

            _todayAiMessageCount.value += 1
            _chatMessages.value = _chatMessages.value + ChatMessage(sender = "AI", text = aiResponse)
            _isChatLoading.value = false
        }
    }

    // AI Study Planner & Dynamic Goal Creator
    fun generateStudyPlan() {
        val isUserPremium = user.value?.isPremium ?: false
        if (!isUserPremium) {
            _studyPlan.value = "পড়াশোনা পরিকল্পনা ও উইকলি রুটিন দেখতে Premium সাবস্ক্রিপশন প্রয়োজন।"
            return
        }

        _isPlanLoading.value = true
        _studyPlan.value = null

        viewModelScope.launch {
            val currentStats = _topicStatsList.value
            val mathAccuracy = currentStats.filter { it.topic.subjectId == "math" }.map { it.accuracyPercentage }.filter { it >= 0 }.average()
            val banglaAccuracy = currentStats.filter { it.topic.subjectId == "bangla" }.map { it.accuracyPercentage }.filter { it >= 0 }.average()
            val englishAccuracy = currentStats.filter { it.topic.subjectId == "english" }.map { it.accuracyPercentage }.filter { it >= 0 }.average()
            val gkAccuracy = currentStats.filter { it.topic.subjectId == "gk" }.map { it.accuracyPercentage }.filter { it >= 0 }.average()

            val statsString = """
                - Math (Percentage & Profit/Loss) = ${if (mathAccuracy.isNaN()) "35" else mathAccuracy.toInt()}%
                - Bangla Literature & Grammar = ${if (banglaAccuracy.isNaN()) "75" else banglaAccuracy.toInt()}%
                - English Vocabulary & Grammar = ${if (englishAccuracy.isNaN()) "60" else englishAccuracy.toInt()}%
                - General Knowledge (BD Affairs) = ${if (gkAccuracy.isNaN()) "70" else gkAccuracy.toInt()}%
            """.trimIndent()

            val prompt = """
                You are a Bangladesh Government Job Exam Coach (expert BCS Coach).
                
                Student Prep Performance:
                $statsString
                
                Please create a detailed coaching response containing exactly:
                1. A comprehensive Weakness Analysis highlighting which topics are below 50% or need urgent focus.
                2. An individualized 7-Day Study Plan with topic distributions.
                3. Clear Daily Goals to improve key weaknesses.
                
                Write the response beautifully formatted in easy-to-read Bangla. Maximize academic encouragement.
            """.trimIndent()

            val planResponse = repository.generateWithGemini(
                prompt = prompt,
                systemInstruction = "You are a specialized BCS Coach. Respond with structured sections in Bangla."
            )
            _studyPlan.value = planResponse
            _isPlanLoading.value = false
        }
    }

    // AI Daily Mission Generator
    fun generateDailyMission() {
        _isMissionLoading.value = true
        _dailyMission.value = null

        viewModelScope.launch {
            val stats = _topicStatsList.value
            val weakTopics = stats.filter { it.difficultyStatus == "WEAK" || it.difficultyStatus == "UNATTEMPTED" }
                .map { it.topic.name }
                .take(3)
                .joinToString(", ")

            val targetWeakness = if (weakTopics.isEmpty()) "শতকরা ও লাভ-ক্ষতি (Math)" else weakTopics

            val prompt = """
                Create a customized single daily mission agenda for a BCS candidate.
                
                Weak Topics / Needs Work:
                - $targetWeakness
                
                Available Daily Study Time:
                2 hours
                
                Focus Language:
                Bangla
                
                Format the answer with clear targets:
                1. 10 MCQs on specific weak topics (e.g. Percentage, Literature, etc.)
                2. Specific textbook section to read
                3. A mini mock test challenge
                Keep it concise and actionable.
            """.trimIndent()

            val missionResponse = repository.generateWithGemini(
                prompt = prompt,
                systemInstruction = "Create a helpful structured daily study list in Bangla."
            )
            _dailyMission.value = missionResponse
            _isMissionLoading.value = false
        }
    }
}
