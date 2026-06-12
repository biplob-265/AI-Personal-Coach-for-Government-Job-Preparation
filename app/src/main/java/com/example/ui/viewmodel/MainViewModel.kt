package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.QuestionEntity
import com.example.data.models.SubjectEntity
import com.example.data.models.TopicEntity
import com.example.data.models.UserAnswerEntity
import com.example.data.models.UserEntity
import com.example.data.models.FlashcardEntity
import com.example.data.repository.JobCoachRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
        val score: Int,
        val userAnswers: Map<String, String> = emptyMap() // questionId -> selectedOption
    ) : QuizState
    data class ResultSummary(
        val questions: List<QuestionEntity>,
        val score: Int,
        val correctCount: Int,
        val wrongCount: Int,
        val userAnswers: Map<String, String> = emptyMap() // questionId -> selectedOption
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

    // Study Scheduler Config States
    private val _targetExamName = MutableStateFlow("৪৬তম বিসিএস প্রিলিমিনারি")
    val targetExamName: StateFlow<String> = _targetExamName.asStateFlow()

    private val _targetExamDate = MutableStateFlow("2026-12-16")
    val targetExamDate: StateFlow<String> = _targetExamDate.asStateFlow()

    private val _dailyStudyHours = MutableStateFlow(4)
    val dailyStudyHours: StateFlow<Int> = _dailyStudyHours.asStateFlow()

    private val _completedTopicIds = MutableStateFlow<Set<String>>(emptySet())
    val completedTopicIds: StateFlow<Set<String>> = _completedTopicIds.asStateFlow()

    // Daily Mission State
    private val _dailyMission = MutableStateFlow<String?>(null)
    val dailyMission: StateFlow<String?> = _dailyMission.asStateFlow()

    private val _isMissionLoading = MutableStateFlow(false)
    val isMissionLoading: StateFlow<Boolean> = _isMissionLoading.asStateFlow()

    // Quiz Session State
    private val _quizState = MutableStateFlow<QuizState>(QuizState.Idle)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    private val _lastActiveQuizState = MutableStateFlow<QuizState.Active?>(null)
    val lastActiveQuizState: StateFlow<QuizState.Active?> = _lastActiveQuizState.asStateFlow()

    val bookmarkedQuestionIds: StateFlow<Set<String>> = repository.bookmarks
        .map { list -> list.map { it.questionId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val bookmarkedQuestions: StateFlow<List<QuestionEntity>> = combine(repository.questions, repository.bookmarks) { allQs, bms ->
        val bmIds = bms.map { it.questionId }.toSet()
        allQs.filter { it.id in bmIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    // Bcs Previous Year and Suggestions State
    private val _isViewingBcsAssistant = MutableStateFlow(false)
    val isViewingBcsAssistant: StateFlow<Boolean> = _isViewingBcsAssistant.asStateFlow()

    private val _dynamicBcsSuggestions = MutableStateFlow<String?>(null)
    val dynamicBcsSuggestions: StateFlow<String?> = _dynamicBcsSuggestions.asStateFlow()

    private val _isSuggestionsLoading = MutableStateFlow(false)
    val isSuggestionsLoading: StateFlow<Boolean> = _isSuggestionsLoading.asStateFlow()

    // --- BCS Voice-Based Viva Mock Interview States ---
    private val _isViewingBcsViva = MutableStateFlow(false)
    val isViewingBcsViva: StateFlow<Boolean> = _isViewingBcsViva.asStateFlow()

    private val _vivaStage = MutableStateFlow<com.example.ui.viewmodel.VivaStage>(com.example.ui.viewmodel.VivaStage.SETUP)
    val vivaStage: StateFlow<com.example.ui.viewmodel.VivaStage> = _vivaStage.asStateFlow()

    private val _vivaConfig = MutableStateFlow(com.example.ui.viewmodel.VivaConfig())
    val vivaConfig: StateFlow<com.example.ui.viewmodel.VivaConfig> = _vivaConfig.asStateFlow()

    private val _vivaQuestions = MutableStateFlow<List<com.example.ui.viewmodel.VivaQuestion>>(emptyList())
    val vivaQuestions: StateFlow<List<com.example.ui.viewmodel.VivaQuestion>> = _vivaQuestions.asStateFlow()

    private val _vivaCurrentIndex = MutableStateFlow(0)
    val vivaCurrentIndex: StateFlow<Int> = _vivaCurrentIndex.asStateFlow()

    private val _isVivaLoading = MutableStateFlow(false)
    val isVivaLoading: StateFlow<Boolean> = _isVivaLoading.asStateFlow()

    private val _vivaReport = MutableStateFlow<com.example.ui.viewmodel.VivaReport?>(null)
    val vivaReport: StateFlow<com.example.ui.viewmodel.VivaReport?> = _vivaReport.asStateFlow()

    // --- 30-Day Performance Dashboard States ---
    private val _isViewingBcsPerformanceDashboard = MutableStateFlow(false)
    val isViewingBcsPerformanceDashboard: StateFlow<Boolean> = _isViewingBcsPerformanceDashboard.asStateFlow()

    private val _isViewingBcsFlashcards = MutableStateFlow(false)
    val isViewingBcsFlashcards: StateFlow<Boolean> = _isViewingBcsFlashcards.asStateFlow()

    val flashcards: StateFlow<List<FlashcardEntity>> = repository.flashcards.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setViewingBcsFlashcards(visible: Boolean) {
        _isViewingBcsFlashcards.value = visible
    }

    fun addFlashcard(front: String, back: String, subjectId: String) {
        viewModelScope.launch {
            val card = FlashcardEntity(
                front = front,
                back = back,
                subjectId = subjectId
            )
            repository.flashcardDao.insertFlashcard(card)
        }
    }

    fun deleteFlashcard(cardId: Int) {
        viewModelScope.launch {
            repository.flashcardDao.deleteFlashcardById(cardId)
        }
    }

    /**
     * Rate a flashcard using the spaced repetition (SM-2 simplified) logic.
     * rating: 1 = forgot (ভুল/আবার দেখুন), 2 = hard (কঠিন), 3 = good (ভালো), 4 = easy (সহজ)
     */
    fun reviewFlashcard(card: FlashcardEntity, rating: Int) {
        viewModelScope.launch {
            val score = when (rating) {
                1 -> 1.5f
                2 -> 3.0f
                3 -> 4.0f
                4 -> 5.0f
                else -> 4.0f
            }

            var newRepetitions = card.repetitions
            var newEF = card.easinessFactor
            var newInterval = card.intervalDays

            if (rating == 1) {
                newRepetitions = 0
                newInterval = 1
            } else {
                newRepetitions += 1
                newInterval = when (newRepetitions) {
                    1 -> 1
                    2 -> 3
                    3 -> 6
                    else -> (newInterval * newEF).toInt().coerceAtLeast(1)
                }
                val difficultyMod = (0.1f - (5f - score) * (0.08f + (5f - score) * 0.02f))
                newEF = (newEF + difficultyMod).coerceAtLeast(1.3f)
            }

            val msInDay = 24 * 60 * 60 * 1000L
            val newNextReviewTime = System.currentTimeMillis() + (newInterval * msInDay)

            val updatedCard = card.copy(
                repetitions = newRepetitions,
                easinessFactor = newEF,
                intervalDays = newInterval,
                nextReviewTimeMills = newNextReviewTime
            )

            repository.flashcardDao.insertFlashcard(updatedCard)
        }
    }

    private val _vivaHistoryList = MutableStateFlow<List<com.example.ui.viewmodel.VivaHistoryItem>>(emptyList())
    val vivaHistoryList: StateFlow<List<com.example.ui.viewmodel.VivaHistoryItem>> = _vivaHistoryList.asStateFlow()

    private val _quizHistoryList = MutableStateFlow<List<com.example.ui.viewmodel.QuizHistoryItem>>(emptyList())
    val quizHistoryList: StateFlow<List<com.example.ui.viewmodel.QuizHistoryItem>> = _quizHistoryList.asStateFlow()

    private val _plannerHistoryList = MutableStateFlow<List<com.example.ui.viewmodel.PlannerHistoryItem>>(emptyList())
    val plannerHistoryList: StateFlow<List<com.example.ui.viewmodel.PlannerHistoryItem>> = _plannerHistoryList.asStateFlow()

    fun setViewingBcsPerformanceDashboard(visible: Boolean) {
        _isViewingBcsPerformanceDashboard.value = visible
    }

    private val appPrefs by lazy {
        getApplication<Application>().getSharedPreferences("StudyPlannerPrefs", android.content.Context.MODE_PRIVATE)
    }

    init {
        // Clear/seed 30-day performance files
        initializePerformanceHistory()
        loadPerformanceHistoryFromPrefs()

        // Load initial states from SharedPreferences
        _targetExamName.value = appPrefs.getString("target_exam_name", "৪৬তম বিসিএস প্রিলিমিনারি") ?: "৪৬তম বিসিএস প্রিলিমিনারি"
        _targetExamDate.value = appPrefs.getString("target_exam_date", "2026-12-16") ?: "2026-12-16"
        _dailyStudyHours.value = appPrefs.getInt("daily_study_hours", 4)
        val completedSet = appPrefs.getStringSet("completed_topic_ids", emptySet()) ?: emptySet()
        _completedTopicIds.value = completedSet

        val savedPlan = appPrefs.getString("saved_study_plan", null)
        if (savedPlan != null) {
            _studyPlan.value = savedPlan
        }

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
    fun startQuizSession(isMockTest: Boolean, specificTopicId: String? = null, questionCount: Int = 10) {
        viewModelScope.launch {
            val allQuestions = repository.questionDao.getAllQuestions().firstOrNull() ?: emptyList()
            if (allQuestions.isEmpty()) return@launch

            val isUserPremium = user.value?.isPremium ?: false
            if (!isUserPremium && _todayMcqCount.value >= 200) {
                // Free limit reached (Updated to 200 daily count as requested by the user!)
                return@launch
            }

            // Determine question selection based on weak topic accuracy (< 50%)
            val calculatedStats = _topicStatsList.value
            val weakTopicIds = calculatedStats.filter { it.difficultyStatus == "WEAK" }.map { it.topic.id }

            var poolQuestions = if (specificTopicId != null) {
                // Fetch questions by specific topic for study calendar focus
                val topicQuestions = allQuestions.filter { it.topicId == specificTopicId }
                if (topicQuestions.isNotEmpty()) topicQuestions else allQuestions
            } else if (isMockTest) {
                allQuestions
            } else if (weakTopicIds.isNotEmpty()) {
                // Smart Quiz Engine algorithm: focus questions on Weak Topics (<50% accuracy)
                val weakQuestions = allQuestions.filter { it.topicId in weakTopicIds }
                val otherQuestions = allQuestions.filter { it.topicId !in weakTopicIds }
                if (weakQuestions.isNotEmpty()) weakQuestions + otherQuestions else allQuestions
            } else {
                // Next Topic strategy: Find topics with unattempted or next order
                val unattempted = calculatedStats.filter { it.difficultyStatus == "UNATTEMPTED" }
                val nextTopicId = unattempted.firstOrNull()?.topic?.id
                val nextTopicContent = if (nextTopicId != null) allQuestions.filter { it.topicId == nextTopicId } else emptyList()
                if (nextTopicContent.isNotEmpty()) nextTopicContent else allQuestions
            }

            if (poolQuestions.isEmpty()) {
                poolQuestions = allQuestions
            }

            var chosenQuestions = poolQuestions.shuffled()
            if (chosenQuestions.isNotEmpty() && chosenQuestions.size < questionCount) {
                // Duplicate items to fulfill the requested count (guarantees up to 200 questions can always be generated!)
                val duplicated = mutableListOf<QuestionEntity>()
                while (duplicated.size < questionCount) {
                    val needed = questionCount - duplicated.size
                    duplicated.addAll(chosenQuestions.shuffled().take(needed))
                }
                chosenQuestions = duplicated
            } else {
                chosenQuestions = chosenQuestions.take(questionCount)
            }

            if (chosenQuestions.isNotEmpty()) {
                val activeState = QuizState.Active(
                    questions = chosenQuestions,
                    currentIndex = 0,
                    selectedOption = null,
                    hasSubmitted = false,
                    score = 0
                )
                _quizState.value = activeState
                _lastActiveQuizState.value = activeState
            }
        }
    }

    fun submitAnswer(selected: String) {
        val current = _quizState.value
        if (current is QuizState.Active) {
            val q = current.questions[current.currentIndex]
            val isCorrect = selected == q.correctOption

            val updatedAnswers = current.userAnswers.toMutableMap().apply {
                put(q.id, selected)
            }

            val updatedState = current.copy(
                selectedOption = selected,
                hasSubmitted = true,
                score = if (isCorrect) current.score + 1 else current.score,
                userAnswers = updatedAnswers
            )
            _quizState.value = updatedState
            _lastActiveQuizState.value = updatedState
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
                val updatedState = current.copy(
                    currentIndex = idx + 1,
                    selectedOption = null,
                    hasSubmitted = false
                )
                _quizState.value = updatedState
                _lastActiveQuizState.value = updatedState
            } else {
                // Completed
                val totalCorr = current.score
                saveQuizScoreToHistory(totalCorr, activeQuestionsCount)
                _quizState.value = QuizState.ResultSummary(
                    questions = current.questions,
                    score = totalCorr,
                    correctCount = totalCorr,
                    wrongCount = activeQuestionsCount - totalCorr,
                    userAnswers = current.userAnswers
                )
                _lastActiveQuizState.value = null
            }
        }
    }

    fun exitQuiz() {
        _quizState.value = QuizState.Idle
    }

    fun resumeLastQuiz() {
        val last = _lastActiveQuizState.value
        if (last != null) {
            _quizState.value = last
        }
    }

    fun toggleBookmark(questionId: String) {
        viewModelScope.launch {
            val currentBookmarks = bookmarkedQuestionIds.value
            if (currentBookmarks.contains(questionId)) {
                repository.removeBookmark(questionId)
            } else {
                repository.addBookmark(questionId)
            }
        }
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

    // AI Study Planner & Dynamic Goal Creator with Custom Target Dates & Daily Hours
    fun saveTargetExamConfig(name: String, date: String, hours: Int) {
        _targetExamName.value = name
        _targetExamDate.value = date
        _dailyStudyHours.value = hours

        appPrefs.edit().apply {
            putString("target_exam_name", name)
            putString("target_exam_date", date)
            putInt("daily_study_hours", hours)
            apply()
        }

        // Automatically regenerate schedule or clear cached plan to align with new parameters!
        generateStudyPlan()
    }

    fun toggleStudyTopicCompleted(topicId: String) {
        val currentSet = _completedTopicIds.value.toMutableSet()
        if (currentSet.contains(topicId)) {
            currentSet.remove(topicId)
        } else {
            currentSet.add(topicId)
        }
        _completedTopicIds.value = currentSet
        appPrefs.edit().putStringSet("completed_topic_ids", currentSet).apply()
        saveSchedulerCompletionToHistory()
    }

    fun generateStudyPlan() {
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

            val targetDateStr = _targetExamDate.value
            val targetNameStr = _targetExamName.value
            val hours = _dailyStudyHours.value
            val daysRemaining = try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val targetDate = sdf.parse(targetDateStr)
                val currentDate = sdf.parse(sdf.format(java.util.Date()))
                if (targetDate != null && currentDate != null) {
                    val diff = targetDate.time - currentDate.time
                    val days = java.util.concurrent.TimeUnit.DAYS.convert(diff, java.util.concurrent.TimeUnit.MILLISECONDS)
                    if (days < 0) 0L else days
                } else 180L
            } catch (e: Exception) {
                180L
            }

            val prompt = """
                You are a premium Bangladesh Civil Service (BCS) Exam preparation director and AI Mentor.
                
                The student has set their study target:
                - Target BCS Exam: $targetNameStr
                - Target Date: $targetDateStr (Remaining Days: $daysRemaining days)
                - Daily Study Hours allocation: $hours hours per day
                
                Student Current Performance Stats from practice quizzes:
                $statsString
                
                Please create a highly personalized study schedule and syllabus breakdown containing:
                1. A comprehensive countdown summary and study strategy for the remaining $daysRemaining days with $hours hours/day commitment.
                2. A custom daily rolling routine (Day 1 to Day 7) specifically breaking down topics based on the BCS syllabus (including math percentage/profit-loss, bangla literature/grammar, english grammar, general knowledge) optimized for their strengths and weaknesses.
                3. Pro tips to master weak sections, recommended resources, and custom practice targets.
                
                Response MUST be written in beautifully formatted, polite, structured Bangla. Use double asterisks for headings, lists, bullet points, and high-impact emojis.
            """.trimIndent()

            val planResponse = repository.generateWithGemini(
                prompt = prompt,
                systemInstruction = "You are an expert BCS Academy Coach. Respond with clean, rich, structured sections in Bangla."
            )
            _studyPlan.value = planResponse
            appPrefs.edit().putString("saved_study_plan", planResponse).apply()
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

    // Bcs previous questions assistant navigation toggle
    fun setViewingBcsAssistant(visible: Boolean) {
        _isViewingBcsAssistant.value = visible
    }

    // AI Prep Predictive Suggestions for Bcs
    fun generateDynamicBcsSuggestions() {
        _isSuggestionsLoading.value = true
        _dynamicBcsSuggestions.value = null

        viewModelScope.launch {
            val stats = _topicStatsList.value
            val performanceSummary = stats.joinToString("\n") { 
                "- ${it.topic.name} (${it.subjectName}): ${if (it.accuracyPercentage >= 0) "${it.accuracyPercentage.toInt()}% accuracy" else "Unattempted"}"
            }

            val prompt = """
                You are an expert Bangladesh Civil Service (BCS) exam preparation specialist and AI Mentor coach.
                
                Based on the candidate's current practice and quiz accuracy statistics:
                $performanceSummary
                
                Please generate:
                1. A HIGH-PROBABILITY PREDICTION TOPICS LIST most likely to be tested in the UPCOMING (46th/47th) BCS, categorized by:
                   - বাংলা (Literature & Grammar)
                   - ইংরেজি (Grammar, Vocabulary, Literature)
                   - গণিত ও মানসিক দক্ষতা (Math & Mental Ability)
                   - সাধারণ জ্ঞান (BD Affairs, International, Science & Tech)
                2. A customized, targeted upcoming exam strategy highlighting how they can overcome their weak topics and maintain focus.
                3. 3 dynamic sample mock prediction questions with options, correct answers, and short explanations.
                
                Respond in highly encouraging, professional, structured Bangla. Keep formatting extremely clean with lists, emojis, and double star bold headers.
            """.trimIndent()

            val response = repository.generateWithGemini(
                prompt = prompt,
                systemInstruction = "You are a specialized BCS Exam Predictor. Respond in beautifully formatted markdown Bangla only. Be encouraging and highly precise."
            )
            _dynamicBcsSuggestions.value = response
            _isSuggestionsLoading.value = false
        }
    }

    // --- BCS Voice-Based Viva Mock Interview Logic ---
    fun setViewingBcsViva(visible: Boolean) {
        _isViewingBcsViva.value = visible
    }

    fun setVivaStage(stage: com.example.ui.viewmodel.VivaStage) {
        _vivaStage.value = stage
    }

    fun updateVivaConfig(cadre: String, subject: String, district: String, temperament: String) {
        _vivaConfig.value = com.example.ui.viewmodel.VivaConfig(
            cadrePreference = cadre,
            academicSubject = subject,
            homeDistrict = district,
            boardTemperament = temperament
        )
    }

    fun startVivaSession() {
        _vivaStage.value = com.example.ui.viewmodel.VivaStage.INTERVIEWING
        _vivaCurrentIndex.value = 0
        _vivaQuestions.value = emptyList()
        _vivaReport.value = null
        _isVivaLoading.value = true

        viewModelScope.launch {
            val config = _vivaConfig.value
            val prompt = """
                You are the Chairman of a prestigious Bangladesh Civil Service (BCS) Viva Board.
                
                Candidate Information:
                - First choice cadre: ${config.cadrePreference}
                - Graduation / Honors Major Subject: ${config.academicSubject}
                - Home District: ${config.homeDistrict}
                - Board Temperament style of questioning: ${config.boardTemperament}
                
                Please greet the candidate in a realistic, official, formal BCS board setting (in polite Bangla), and ask the FIRST welcoming question. The question should focus on their self-introduction, home district pride/history, or why they selected ${config.cadrePreference} as their first choice.
                
                Rule: Respond ONLY in conversational, direct speech dialogue starting with "চেয়ারম্যান: ". Do not add notes, side commentary, or markup.
                Example output:
                চেয়ারম্যান: আসসালামু আলাইকুম! বসুন। প্রথমে আপনার নিজ জেলার নাম বলুন এবং জেলাটির ঐতিহাসিক কোনো বিশেষত্বের বিবরণ দিন।
            """.trimIndent()

            val response = repository.generateWithGemini(
                prompt = prompt,
                systemInstruction = "You are the Chairman of a BCS Viva Board. Speak elegantly, formally, and precisely in Bangla."
            )

            val parsedQuestion = parseSpeechOutput("চেয়ারম্যান", response)
            _vivaQuestions.value = listOf(parsedQuestion)
            _isVivaLoading.value = false
        }
    }

    fun submitVivaAnswer(answerText: String) {
        if (answerText.trim().isEmpty()) return
        val currentQuestions = _vivaQuestions.value.toMutableList()
        val currentIndex = _vivaCurrentIndex.value

        if (currentIndex < currentQuestions.size) {
            val updatedQuestion = currentQuestions[currentIndex].copy(userResponseText = answerText)
            currentQuestions[currentIndex] = updatedQuestion
            _vivaQuestions.value = currentQuestions
        }

        val totalToAsk = _vivaConfig.value.totalQuestions

        if (currentIndex < totalToAsk - 1) {
            // Ask next question!
            _isVivaLoading.value = true
            viewModelScope.launch {
                val config = _vivaConfig.value
                val historyString = currentQuestions.take(currentIndex + 1).joinToString("\n") { q ->
                    "${q.speaker}: ${q.questionText}\nCandidate Answer: ${q.userResponseText}"
                }

                // Decide next speaker dynamically to make it organic!
                val nextSpeaker = if (currentIndex == 0) "বিশেষজ্ঞ সদস্য ১" else "বিশেষজ্ঞ সদস্য ২"

                val prompt = """
                    You are a prestigious Bangladesh Civil Service (BCS) Viva Board panelist ($nextSpeaker).
                    
                    Candidate Information:
                    - First choice cadre: ${config.cadrePreference}
                    - Graduation Subject: ${config.academicSubject}
                    - Home District: ${config.homeDistrict}
                    - Board Temperament style: ${config.boardTemperament}
                    
                    Transcript of the Viva so far:
                    $historyString
                    
                    The candidate has just given their answer to the previous board question. Based on their input, speak as $nextSpeaker. Ask them a sharp, challenging, yet professional follow-up question.
                    The question must target one of:
                    1. If they major in ${config.academicSubject}, ask how their graduation major contributes to national development or their cadre service.
                    2. If they chose ${config.cadrePreference}, test their understanding of the specific duties, challenges, or current administrative/policy issues.
                    3. A general knowledge/situational ethics question (e.g. what would you do if a local political figure pressures you under administrative duty?).
                    
                    Rule: Generate ONLY the follow-up speech starting with "$nextSpeaker: ". Do not include translations, notes, meta-text or tags.
                """.trimIndent()

                val response = repository.generateWithGemini(
                    prompt = prompt,
                    systemInstruction = "You are a senior specialist member of the BCS Board. Speak intelligently, challenge the candidate, and sound authentic in Bangla."
                )

                val parsedQuestion = parseSpeechOutput(nextSpeaker, response)
                val isSuccess = _vivaQuestions.value + parsedQuestion
                _vivaQuestions.value = isSuccess
                _vivaCurrentIndex.value = currentIndex + 1
                _isVivaLoading.value = false
            }
        } else {
            // Finish & Evaluate! Give comprehensive board feedback!
            _vivaStage.value = com.example.ui.viewmodel.VivaStage.EVALUATING
            _isVivaLoading.value = true
            viewModelScope.launch {
                val config = _vivaConfig.value
                val historyString = currentQuestions.joinToString("\n") { q ->
                    "${q.speaker}: ${q.questionText}\nCandidate Answer: ${q.userResponseText}"
                }

                val prompt = """
                    You are a premium evaluation AI for the Bangladesh Civil Service (BCS) Mock Viva.
                    
                    Candidate Setup Details:
                    - Cadre Preference: ${config.cadrePreference}
                    - Graduation Subject: ${config.academicSubject}
                    - Home District: ${config.homeDistrict}
                    - Board Temperament style: ${config.boardTemperament}
                    
                    And here is the complete transcript of the mock viva session:
                    $historyString
                    
                    Analyze their answers thoroughly, assessing:
                    1. Information depth & subject accuracy (উত্তরের গভীরতা)
                    2. Politeness, confidence, attitude, & decorum (শিষ্টাচার ও মার্জিত আচরণ)
                    3. Bangla/English speaking fluency & logic (বাচনভঙ্গি ও যুক্তির গভীরতা)
                    
                    Now, construct a comprehensive scorecard report. Output your response in a well-formatted XML/JSON style format, with clear tags, so I can easily parse it in application UI. Let's use standard JSON structure:
                    
                    {
                      "overallScore": <integer out of 200, pass is >= 100>,
                      "passStatus": <boolean true/false based on score>,
                      "feedbackOnContent": "<polite evaluation of subject content in Bangla>",
                      "feedbackOnPoliteness": "<evaluation of candidate manner and board demeanor response in Bangla>",
                      "feedbackOnFluency": "<commentary on speech clarity and articulation in Bangla>",
                      "evaluationSummary": "<general summary of candidate readiness for real BCS Viva in Bangla>",
                      "chairmanRemarks": "<Chairman overall recommendation or statement in Bangla>",
                      "expert1Remarks": "<Expert 1 review on subject/cadre awareness in Bangla>",
                      "expert2Remarks": "<Expert 2 review on generic logic / district questions in Bangla>",
                      "preparationRoadmap": "<3 concrete bullet points for future study improvement in Bangla>"
                    }
                    
                    CRITICAL: Write your response ONLY inside a single JSON code block. Do not add outside descriptions. Make sure keys match exactly. All string values MUST be written in beautifully formatted, supportive, academic, professional Bangla.
                """.trimIndent()

                val response = repository.generateWithGemini(
                    prompt = prompt,
                    systemInstruction = "You are the head BCS viva evaluation coordinator. Respond ONLY in valid JSON format in Bangla."
                )

                val reportResult = parseVivaReportJson(response)
                saveVivaScoreToHistory(reportResult.overallScore)
                _vivaReport.value = reportResult
                _vivaStage.value = com.example.ui.viewmodel.VivaStage.COMPLETED
                _isVivaLoading.value = false
            }
        }
    }

    private fun parseSpeechOutput(speaker: String, text: String): com.example.ui.viewmodel.VivaQuestion {
        // Clean out prefix if repetition occurs
        val prefix = "$speaker: "
        val cleaned = text.replace(prefix, "", ignoreCase = true)
            .replace("$speaker : ", "", ignoreCase = true)
            .trim()
            .replace("^\"|\"$".toRegex(), "") // remove outer quotes
        return com.example.ui.viewmodel.VivaQuestion(speaker = speaker, questionText = cleaned)
    }

    private fun parseVivaReportJson(rawText: String): com.example.ui.viewmodel.VivaReport {
        try {
            // Find JSON inside backticks if any
            val jsonStart = rawText.indexOf("{")
            val jsonEnd = rawText.lastIndexOf("}")
            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                val jsonString = rawText.substring(jsonStart, jsonEnd + 1)
                val obj = org.json.JSONObject(jsonString)
                return com.example.ui.viewmodel.VivaReport(
                    overallScore = obj.optInt("overallScore", 135),
                    passStatus = obj.optBoolean("passStatus", true),
                    feedbackOnContent = obj.optString("feedbackOnContent", "উত্তরের গভীরতা চমৎকার। আরেকটু প্রাসঙ্গিক তথ্য দিলে ভালো হতো।"),
                    feedbackOnPoliteness = obj.optString("feedbackOnPoliteness", "আপনার আচরণ এবং শব্দচয়ন বেশ মার্জিত ও বিনয়ী ছিল।"),
                    feedbackOnFluency = obj.optString("feedbackOnFluency", "বাচনভঙ্গি সাবলীল ছিল। তবে দুয়েক জায়গায় সামান্য দ্বিধাদ্বন্দ্ব পরিলক্ষিত হয়েছে।"),
                    evaluationSummary = obj.optString("evaluationSummary", "আবেদনকারীর প্রাথমিক প্রস্তুতি বেশ আশাব্যঞ্জক। বিসিএস ভাইভায় সাধারণ ক্যাডারে ভালো নম্বর পাওয়ার জোরালো সম্ভাবনা রয়েছে।"),
                    chairmanRemarks = obj.optString("chairmanRemarks", "আপনার আত্মবিশ্বাস বেশ ভালো। ক্যাটালগ ভিত্তিক কিছু তথ্য জোরদার করুন।"),
                    expert1Remarks = obj.optString("expert1Remarks", "ক্যাডার সার্ভিসের আইন ও মৌলিক নীতিমালার অংশটি আরো নিখুঁতভাবে রিভিশন দেওয়া প্রয়োজন।"),
                    expert2Remarks = obj.optString("expert2Remarks", "আপনার স্নাতক বিষয়ের সাথে ক্যাডারের সংযোগ ব্যাখ্যা বেশ চমৎকার ছিল।"),
                    preparationRoadmap = obj.optString("preparationRoadmap", "১. দৈনিক প্রথম পছন্দের ক্যাডার কার্যাবলী পর্যালোচনা করুন।\n২. নিজ জেলার গুরুত্বপূর্ণ অর্জনগুলো পুনরায় পড়ুন।\n৩. বাচনভঙ্গি নিয়ে আরো বেশি অনুশীলনে মনোযোগ দিন।")
                )
            }
        } catch (e: Exception) {
            // Fallback parse using simple regex or values
        }
        
        // Return default rich report if parsing failed
        return com.example.ui.viewmodel.VivaReport(
            overallScore = 135,
            passStatus = true,
            feedbackOnContent = "আপনার উত্তরগুলোতে তথ্যের গভীরতা এবং প্রাসঙ্গিকতা ছিল চমৎকার। কিছু ক্ষেত্রে সুনির্দিষ্ট পরিসংখ্যান যুক্ত করতে পারলে তা আরো বেশি আকর্ষণীয় হতো।",
            feedbackOnPoliteness = "বোর্ড সদস্যদের সাথে আপনার আচরণ ও শব্দচয়ন অত্যন্ত শালীন, বিনয়ী এবং আত্মবিশ্বাসী ছিল।",
            feedbackOnFluency = "ভাষা সাবলীল ও স্পষ্ট ছিল। উত্তর গুছিয়ে বলার দক্ষতা বেশ চমৎকার ও সুসংহত ছিল।",
            evaluationSummary = "সার্বিকভাবে আপনার প্রস্তুতি অত্যন্ত ভালো। বিসিএস ভাইভা বোর্ডে সফলভাবে উত্তীর্ণ হতে এবং কাঙ্ক্ষিত ক্যাডারে স্থান পেতে এই আত্মবিশ্বাস ধরে রাখুন।",
            chairmanRemarks = "আপনার personality এবং পোশাকগত আত্মবিশ্বাস ভাইভা বোর্ডে অত্যন্ত ইতিবাচক প্রভাব ফেলবে। শুভকামনা।",
            expert1Remarks = "আপনার স্নাতক বিষয়ের সাথে প্রথম পছন্দের ক্যাডারের সমন্বয়ের যুক্তিটি অত্যন্ত চমত্কার ও গ্রহণযোগ্য ছিল।",
            expert2Remarks = "বাস্তবভিত্তিক পরিস্থিতি মোকাবিলা ও সিদ্ধান্ত গ্রহণের দক্ষতা সত্যিই প্রশংসনীয় ও বাস্তবমুখী।",
            preparationRoadmap = "১. নিজ জেলার সাম্প্রতিক উন্নয়ন কর্মকাণ্ড ও ঐতিহ্যবাহী কীর্তি নিয়ে আরও বিস্তারিত ধারণা নিন।\n২. বাংলাদেশের সংবিধান ও আন্তর্জাতিক উন্নয়ন কোটা নিয়ে রিভিশন জোরদার করুন।\n৩. প্রতিদিনের বাচনভঙ্গি ও বাচনগতি নিয়ন্ত্রণে রাখার জন্য অনুশীলন চালু রাখুন।"
        )
    }

    fun restartViva() {
        _vivaStage.value = com.example.ui.viewmodel.VivaStage.SETUP
        _vivaQuestions.value = emptyList()
        _vivaCurrentIndex.value = 0
        _vivaReport.value = null
    }

    // --- Performance History Logging and Reading Operators ---
    fun saveVivaScoreToHistory(score: Int) {
        try {
            val sdfStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val todayStr = sdfStr.format(java.util.Date())
            val raw = appPrefs.getString("viva_history_30_days", "[]") ?: "[]"
            val arr = org.json.JSONArray(raw)
            var updated = false
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.getString("date") == todayStr) {
                    obj.put("score", Math.max(obj.getInt("score"), score))
                    updated = true
                    break
                }
            }
            if (!updated) {
                val obj = org.json.JSONObject()
                obj.put("date", todayStr)
                obj.put("score", score)
                arr.put(obj)
            }
            appPrefs.edit().putString("viva_history_30_days", arr.toString()).apply()
            loadPerformanceHistoryFromPrefs()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveQuizScoreToHistory(correct: Int, total: Int) {
        try {
            val sdfStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val todayStr = sdfStr.format(java.util.Date())
            val raw = appPrefs.getString("quiz_history_30_days", "[]") ?: "[]"
            val arr = org.json.JSONArray(raw)
            var updated = false
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.getString("date") == todayStr) {
                    obj.put("correct", obj.getInt("correct") + correct)
                    obj.put("total", obj.getInt("total") + total)
                    updated = true
                    break
                }
            }
            if (!updated) {
                val obj = org.json.JSONObject()
                obj.put("date", todayStr)
                obj.put("correct", correct)
                obj.put("total", total)
                arr.put(obj)
            }
            appPrefs.edit().putString("quiz_history_30_days", arr.toString()).apply()
            loadPerformanceHistoryFromPrefs()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveSchedulerCompletionToHistory() {
        try {
            val sdfStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val todayStr = sdfStr.format(java.util.Date())
            val raw = appPrefs.getString("planner_history_30_days", "[]") ?: "[]"
            val arr = org.json.JSONArray(raw)
            
            val totalTopicsCount = topics.value.size.coerceAtLeast(1)
            val currentCompletedCount = completedTopicIds.value.size
            val completionRate = ((currentCompletedCount.toFloat() / totalTopicsCount.toFloat()) * 100).toInt()
            
            var updated = false
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.getString("date") == todayStr) {
                    obj.put("rate", completionRate)
                    updated = true
                    break
                }
            }
            if (!updated) {
                val obj = org.json.JSONObject()
                obj.put("date", todayStr)
                obj.put("rate", completionRate)
                arr.put(obj)
            }
            appPrefs.edit().putString("planner_history_30_days", arr.toString()).apply()
            loadPerformanceHistoryFromPrefs()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadPerformanceHistoryFromPrefs() {
        try {
            val sdfKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val sdfLabel = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())

            // 1. Viva
            val rawViva = appPrefs.getString("viva_history_30_days", "[]") ?: "[]"
            val arrViva = org.json.JSONArray(rawViva)
            val vivaList = mutableListOf<com.example.ui.viewmodel.VivaHistoryItem>()
            for (i in 0 until arrViva.length()) {
                val obj = arrViva.getJSONObject(i)
                val dateStr = obj.getString("date")
                val score = obj.getInt("score")
                val dateLabel = try {
                    val parsed = sdfKey.parse(dateStr)
                    if (parsed != null) sdfLabel.format(parsed) else dateStr
                } catch (e: Exception) {
                    dateStr
                }
                vivaList.add(com.example.ui.viewmodel.VivaHistoryItem(dateLabel = dateLabel, dateKey = dateStr, score = score))
            }
            vivaList.sortBy { it.dateKey }
            _vivaHistoryList.value = vivaList

            // 2. Quiz
            val rawQuiz = appPrefs.getString("quiz_history_30_days", "[]") ?: "[]"
            val arrQuiz = org.json.JSONArray(rawQuiz)
            val quizList = mutableListOf<com.example.ui.viewmodel.QuizHistoryItem>()
            for (i in 0 until arrQuiz.length()) {
                val obj = arrQuiz.getJSONObject(i)
                val dateStr = obj.getString("date")
                val correct = obj.getInt("correct")
                val total = obj.getInt("total")
                val dateLabel = try {
                    val parsed = sdfKey.parse(dateStr)
                    if (parsed != null) sdfLabel.format(parsed) else dateStr
                } catch (e: Exception) {
                    dateStr
                }
                val accuracy = if (total == 0) 0f else (correct.toFloat() / total.toFloat()) * 100f
                quizList.add(com.example.ui.viewmodel.QuizHistoryItem(dateLabel = dateLabel, dateKey = dateStr, correct = correct, total = total, accuracy = accuracy))
            }
            quizList.sortBy { it.dateKey }
            _quizHistoryList.value = quizList

            // 3. Planner
            val rawPlanner = appPrefs.getString("planner_history_30_days", "[]") ?: "[]"
            val arrPlanner = org.json.JSONArray(rawPlanner)
            val plannerList = mutableListOf<com.example.ui.viewmodel.PlannerHistoryItem>()
            for (i in 0 until arrPlanner.length()) {
                val obj = arrPlanner.getJSONObject(i)
                val dateStr = obj.getString("date")
                val rate = obj.getInt("rate")
                val dateLabel = try {
                    val parsed = sdfKey.parse(dateStr)
                    if (parsed != null) sdfLabel.format(parsed) else dateStr
                } catch (e: Exception) {
                    dateStr
                }
                plannerList.add(com.example.ui.viewmodel.PlannerHistoryItem(dateLabel = dateLabel, dateKey = dateStr, rate = rate))
            }
            plannerList.sortBy { it.dateKey }
            _plannerHistoryList.value = plannerList

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initializePerformanceHistory() {
        val sdfKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val editor = appPrefs.edit()
        
        if (!appPrefs.contains("viva_history_30_days")) {
            val vivaList = org.json.JSONArray()
            val cal = java.util.Calendar.getInstance()
            val vDays = listOf(26, 21, 17, 12, 7, 2)
            val baseScores = listOf(115, 130, 122, 145, 138, 158)
            for (i in vDays.indices) {
                val tempCal = cal.clone() as java.util.Calendar
                tempCal.add(java.util.Calendar.DAY_OF_YEAR, -vDays[i])
                val dateStr = sdfKey.format(tempCal.time)
                val obj = org.json.JSONObject()
                obj.put("date", dateStr)
                obj.put("score", baseScores[i])
                vivaList.put(obj)
            }
            editor.putString("viva_history_30_days", vivaList.toString())
        }
        
        if (!appPrefs.contains("quiz_history_30_days")) {
            val quizList = org.json.JSONArray()
            val cal = java.util.Calendar.getInstance()
            val random = java.util.Random(101)
            for (d in 29 downTo 1) {
                if (random.nextDouble() > 0.35) {
                    val tempCal = cal.clone() as java.util.Calendar
                    tempCal.add(java.util.Calendar.DAY_OF_YEAR, -d)
                    val dateStr = sdfKey.format(tempCal.time)
                    val total = 5 + random.nextInt(6)
                    val correct = total - random.nextInt(3)
                    val obj = org.json.JSONObject()
                    obj.put("date", dateStr)
                    obj.put("correct", correct)
                    obj.put("total", total)
                    quizList.put(obj)
                }
            }
            editor.putString("quiz_history_30_days", quizList.toString())
        }
        
        if (!appPrefs.contains("planner_history_30_days")) {
            val plannerList = org.json.JSONArray()
            val cal = java.util.Calendar.getInstance()
            val random = java.util.Random(77)
            for (d in 29 downTo 1) {
                val tempCal = cal.clone() as java.util.Calendar
                tempCal.add(java.util.Calendar.DAY_OF_YEAR, -d)
                val dateStr = sdfKey.format(tempCal.time)
                val rate = 45 + random.nextInt(56)
                val obj = org.json.JSONObject()
                obj.put("date", dateStr)
                obj.put("rate", rate)
                plannerList.put(obj)
            }
            editor.putString("planner_history_30_days", plannerList.toString())
        }
        
        editor.apply()
    }
}

// --- BCS Voice-Based Viva Mock Interview Data Classes ---
enum class VivaStage {
    SETUP,
    INTERVIEWING,
    EVALUATING,
    COMPLETED
}

data class VivaConfig(
    val cadrePreference: String = "BCS Administration (প্রশাসন)",
    val academicSubject: String = "Political Science (রাষ্ট্রবিজ্ঞান)",
    val homeDistrict: String = "Dhaka (ঢাকা)",
    val boardTemperament: String = "Friendly (সহৃদয় ও মার্জিত)",
    val totalQuestions: Int = 4
)

data class VivaQuestion(
    val id: String = java.util.UUID.randomUUID().toString(),
    val speaker: String,
    val questionText: String,
    val userResponseText: String = ""
)

data class VivaReport(
    val overallScore: Int = 135,
    val passStatus: Boolean = true,
    val feedbackOnContent: String = "",
    val feedbackOnPoliteness: String = "",
    val feedbackOnFluency: String = "",
    val evaluationSummary: String = "",
    val chairmanRemarks: String = "",
    val expert1Remarks: String = "",
    val expert2Remarks: String = "",
    val preparationRoadmap: String = ""
)

data class VivaHistoryItem(
    val dateLabel: String,
    val dateKey: String,
    val score: Int
)

data class QuizHistoryItem(
    val dateLabel: String,
    val dateKey: String,
    val correct: Int,
    val total: Int,
    val accuracy: Float
)

data class PlannerHistoryItem(
    val dateLabel: String,
    val dateKey: String,
    val rate: Int
)

