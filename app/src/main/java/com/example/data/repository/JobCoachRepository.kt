package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.models.UserEntity
import com.example.data.models.SubjectEntity
import com.example.data.models.TopicEntity
import com.example.data.models.QuestionEntity
import com.example.data.models.UserAnswerEntity
import com.example.data.network.ContentMessage
import com.example.data.network.GenerateContentRequest
import com.example.data.network.Part
import com.example.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JobCoachRepository(context: Context) {

    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "ai_job_coach_db"
    ).build()

    val userDao = db.userDao()
    val subjectDao = db.subjectDao()
    val topicDao = db.topicDao()
    val questionDao = db.questionDao()
    val userAnswerDao = db.userAnswerDao()

    val user: Flow<UserEntity?> = userDao.getUser()
    val subjects: Flow<List<SubjectEntity>> = subjectDao.getAllSubjects()
    val topics: Flow<List<TopicEntity>> = topicDao.getAllTopics()
    val questions: Flow<List<QuestionEntity>> = questionDao.getAllQuestions()
    val answers: Flow<List<UserAnswerEntity>> = userAnswerDao.getAllAnswers()

    // Query to check if database needs seeding, and seed if empty
    suspend fun checkAndSeedDatabase() = withContext(Dispatchers.IO) {
        val qCount = questionDao.getQuestionCount()
        if (qCount == 0) {
            // Seed a local default user profile
            val defaultUser = UserEntity(
                name = "আবেদনকারী", // Applicant/Student
                email = "student@bcsgovprep.bd",
                targetExam = "BCS (৩৮তম-৪৬তম)",
                dailyStudyHours = 2,
                isPremium = false,
                createdAt = getCurrentDateString()
            )
            userDao.insertUser(defaultUser)

            // Seed Subjects
            val listSubjects = listOf(
                SubjectEntity("math", "গণিত"),
                SubjectEntity("bangla", "বাংলা"),
                SubjectEntity("english", "ইংরেজি"),
                SubjectEntity("gk", "সাধারণ জ্ঞান")
            )
            subjectDao.insertSubjects(listSubjects)

            // Seed Topics
            val listTopics = listOf(
                TopicEntity("percentage", "math", "শতকরা (Percentage)"),
                TopicEntity("profit_loss", "math", "লাভ-ক্ষতি (Profit & Loss)"),
                TopicEntity("literature", "bangla", "বাংলা সাহিত্য (Literature)"),
                TopicEntity("grammar", "bangla", "বাংলা ব্যাকরণ (Grammar)"),
                TopicEntity("eng_grammar", "english", "English Grammar"),
                TopicEntity("bd_affairs", "gk", "বাংলাদেশ বিষয়াবলী (BD Affairs)")
            )
            topicDao.insertTopics(listTopics)

            // Seed 12 High-Quality Questions
            val listQuestions = listOf(
                QuestionEntity(
                    id = "q1",
                    question = "১০০ এর ২০% কত?",
                    optionA = "১০", optionB = "২০", optionC = "৩০", optionD = "৪০",
                    correctOption = "B",
                    topicId = "percentage", subjectId = "math",
                    explanation = "১০০ এর ২০% = ১০০ × (২০/১০০) = ২০।"
                ),
                QuestionEntity(
                    id = "q2",
                    question = "কোন সংখ্যার ৬০% থেকে ৬০ বিয়োগ করলে বিয়োগফল ৬০ হবে?",
                    optionA = "১০০", optionB = "২০০", optionC = "২৫০", optionD = "৩০০",
                    correctOption = "B",
                    topicId = "percentage", subjectId = "math",
                    explanation = "ধরি সংখ্যাটি x। প্রশ্নমতে, ৬০% x - ৬০ = ৬০ ⇒ ০.৬০x = ১২০ ⇒ x = ১২০/০.৬০ = ২০০।"
                ),
                QuestionEntity(
                    id = "q3",
                    question = "একটি সংখ্যা অপর একটি সংখ্যার ৮০% হলে, দ্বিতীয় সংখ্যাটি প্রথম সংখ্যার শতকরা কত?",
                    optionA = "১১০%", optionB = "১২০%", optionC = "১২৫%", optionD = "১৫০%",
                    correctOption = "C",
                    topicId = "percentage", subjectId = "math",
                    explanation = "ধরি দ্বিতীয় সংখ্যাটি ১০০। তাহলে প্রথম সংখ্যাটি ৮০। অতএব, দ্বিতীয় সংখ্যাটি প্রথম সংখ্যার (১০০ / ৮০) × ১০০ = ১২৫%।"
                ),
                QuestionEntity(
                    id = "q4",
                    question = "একটি জিনিস ১০০ টাকায় কিনে ১২০ টাকায় বিক্রয় করলে শতকরা কত লাভ হবে?",
                    optionA = "১০%", optionB = "১৫%", optionC = "২০%", optionD = "২৫%",
                    correctOption = "C",
                    topicId = "profit_loss", subjectId = "math",
                    explanation = "লাভ = ১২০ - ১০০ = ২০ টাকা। শতকরা লাভ = (২০ / ১০০) × ১০০% = ২০%।"
                ),
                QuestionEntity(
                    id = "q5",
                    question = "টাকায় ৩টি করে আম ক্রয় করে টাকায় ২টি করে আম বিক্রয় করলে শতকরা কত লাভ হবে?",
                    optionA = "৩০%", optionB = "৪০%", optionC = "৫০%", optionD = "৬০%",
                    correctOption = "C",
                    topicId = "profit_loss", subjectId = "math",
                    explanation = "৩টির ক্রয়মূল্য ১ টাকা হলে ১টির ক্রয়মূল্য ১/৩ টাকা। ২টির বিক্রয়মূল্য ১ টাকা হলে ১টির বিক্রয়মূল্য ১/২ টাকা। লাভ = ১/২ - ১/৩ = ১/৬ টাকা। শতকরা লাভ = (১/৬) / (১/৩) × ১০০ = ৫০%।"
                ),
                QuestionEntity(
                    id = "q6",
                    question = "চর্যাপদ কোন যুগে রচিত হয়েছে?",
                    optionA = "প্রাচীন যুগ", optionB = "মধ্য যুগ", optionC = "আধুনিক যুগ", optionD = "অন্ধকার যুগ",
                    correctOption = "A",
                    topicId = "literature", subjectId = "bangla",
                    explanation = "চর্যাপদ বাংলা সাহিত্যের প্রাচীন যুগের (৬৫০-১২০০ খ্রিষ্টাব্দ) একমাত্র নির্ভরযোগ্য ঐতিহাসিক ও সাহিত্যের নিদর্শন।"
                ),
                QuestionEntity(
                    id = "q7",
                    question = "গীতাঞ্জলি কাব্যের জন্য রবীন্দ্রনাথ ঠাকুর কোন সালে নোবেল পুরস্কার পান?",
                    optionA = "১৯১১", optionB = "১৯১২", optionC = "১৯১৩", optionD = "১৯১৪",
                    correctOption = "C",
                    topicId = "literature", subjectId = "bangla",
                    explanation = "রবীন্দ্রনাথ ঠাকুর ১৯১৩ সালে গীতাঞ্জলি কাব্যের ইংরেজি অনুবাদের জন্য এশিয়ার প্রথম সাহিত্যিক হিসেবে নোবেল লাভ করেন।"
                ),
                QuestionEntity(
                    id = "q8",
                    question = "সন্ধি ব্যাকরণের কোন অংশে আলোচিত বা বিশ্লেষিত হয়?",
                    optionA = "রূপতত্ত্ব (শব্দতত্ত্ব)", optionB = "ধ্বনিতত্ত্ব", optionC = "বাক্যতত্ত্ব (পদক্রম)", optionD = "অর্থতত্ত্ব",
                    correctOption = "B",
                    topicId = "grammar", subjectId = "bangla",
                    explanation = "সন্ধি মানে ২ টি ধ্বনির মিলন। যেহেতু ধ্বনির বিশ্লেষণ ধ্বনিতত্ত্বে হয়ে থাকে, তাই সন্ধিও ধ্বনিতত্ত্বে আলোচিত হয়।"
                ),
                QuestionEntity(
                    id = "q9",
                    question = "Identify the correct sentence:",
                    optionA = "He is senior than me", optionB = "He is senior to me", optionC = "He is senior of me", optionD = "He is senior with me",
                    correctOption = "B",
                    topicId = "eng_grammar", subjectId = "english",
                    explanation = "Latin comparative adjectives (senior, junior, superior, inferior, prior) are followed by 'to' instead of 'than'."
                ),
                QuestionEntity(
                    id = "q10",
                    question = "What is the synonym of the word 'Prudent'?",
                    optionA = "Wise / Cautious", optionB = "Foolish", optionC = "Careless", optionD = "Impatient",
                    correctOption = "A",
                    topicId = "eng_grammar", subjectId = "english",
                    explanation = "Prudent refers to acting or showing care and foresight for the future; hence wise or cautious is the correct synonym."
                ),
                QuestionEntity(
                    id = "q11",
                    question = "বাংলাদেশের মহান স্বাধীনতা দিবস কবে পালন করা হয়?",
                    optionA = "২১শে ফেব্রুয়ারি", optionB = "২৬শে মার্চ", optionC = "১৬ই ডিসেম্বর", optionD = "১৪ই ডিসেম্বর",
                    correctOption = "B",
                    topicId = "bd_affairs", subjectId = "gk",
                    explanation = "২৬শে মার্চ আমাদের মহান স্বাধীনতা ও জাতীয় দিবস এবং ১৬ই ডিসেম্বর আমাদের মহান বিজয় দিবস।"
                ),
                QuestionEntity(
                    id = "q12",
                    question = "বাংলাদেশের সংবিধান গণপ্রজাতন্ত্রী বাংলাদেশ সরকার কর্তৃক কবে গৃহীত হয়?",
                    optionA = "১৬ই ডিসেম্বর ১৯৭২", optionB = "৪ঠা নভেম্বর ১৯৭২", optionC = "২৬শে মার্চ ১৯৭২", optionD = "১৭ই এপ্রিল ১৯৭১",
                    correctOption = "B",
                    topicId = "bd_affairs", subjectId = "gk",
                    explanation = "৪ঠা নভেম্বর ১৯৭২ সালে গণপরিষদে বাংলাদেশের সংবিধান চূড়ান্তভাবে গৃহীত হয় এবং এটি একই বছরের ১৬ই ডিসেম্বর বিজয় দিবসে কার্যকর করা হয়।"
                )
            )
            questionDao.insertQuestions(listQuestions)
        }
    }

    // Call Gemini Model
    suspend fun generateWithGemini(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.trim().isEmpty()) {
            return@withContext "ত্রুটি: এপিআই কী সেট করা হয়নি। অনুগ্রহ করে Google AI Studio-এর Secrets প্যানেল থেকে GEMINI_API_KEY কনফিগার করুন।"
        }

        val contentReq = GenerateContentRequest(
            contents = listOf(
                ContentMessage(parts = listOf(Part(text = prompt)))
            ),
            systemInstruction = systemInstruction?.let {
                ContentMessage(parts = listOf(Part(text = it)))
            }
        )

        try {
            val response = RetrofitClient.service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = contentReq
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "দুঃখিত, কোনো উত্তর পাওয়া যায়নি।"
        } catch (e: Exception) {
            "ত্রুটি: ${e.localizedMessage ?: "নেটওয়ার্ক সংযোগে সমস্যা ঘটেছে।"}"
        }
    }

    // Helper functions
    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Helper to calculate correctness of user answers for each topic
    suspend fun getTopicAccuracySync(topicId: String): Double = withContext(Dispatchers.IO) {
        val answers = userAnswerDao.getAnswersByTopicSync(topicId)
        if (answers.isEmpty()) return@withContext -1.0 // Unattempted
        val correct = answers.count { it.isCorrect }
        return@withContext (correct.toDouble() / answers.size) * 100.0
    }
}
