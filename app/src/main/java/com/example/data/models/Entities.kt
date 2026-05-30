package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String = "local_user",
    val name: String,
    val email: String,
    val targetExam: String = "BCS",
    val dailyStudyHours: Int = 2,
    val isPremium: Boolean = false,
    val createdAt: String = "2026-05-30"
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String, // math, bangla, english, gk
    val name: String // গণিত, বাংলা, ইংরেজি, সাধারণ জ্ঞান
)

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String, // percentage, profit_loss, charjapad, grammar, etc.
    val subjectId: String,
    val name: String // শতকরা, লাভ-ক্ষতি, সাহিত্য, ব্যাকরণ, etc.
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // A, B, C, or D
    val topicId: String,
    val subjectId: String,
    val explanation: String = ""
)

@Entity(tableName = "user_answers")
data class UserAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "local_user",
    val questionId: String,
    val topicId: String,
    val subjectId: String,
    val isCorrect: Boolean,
    val answeredAt: String
)
