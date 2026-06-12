package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.data.models.UserEntity
import com.example.data.models.SubjectEntity
import com.example.data.models.TopicEntity
import com.example.data.models.QuestionEntity
import com.example.data.models.UserAnswerEntity
import com.example.data.models.FlashcardEntity
import com.example.data.models.BookmarkQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun getUser(uid: String = "local_user"): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserSync(uid: String = "local_user"): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET isPremium = :isPremium WHERE uid = :uid")
    suspend fun updatePremiumStatus(isPremium: Boolean, uid: String = "local_user")
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects")
    suspend fun getAllSubjectsSync(): List<SubjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)
}

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId")
    fun getTopicsBySubject(subjectId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId")
    suspend fun getTopicsBySubjectSync(subjectId: String): List<TopicEntity>

    @Query("SELECT * FROM topics")
    suspend fun getAllTopicsSync(): List<TopicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE topicId = :topicId")
    fun getQuestionsByTopic(topicId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE topicId = :topicId")
    suspend fun getQuestionsByTopicSync(topicId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId")
    suspend fun getQuestionsBySubjectSync(subjectId: String): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int
}

@Dao
interface UserAnswerDao {
    @Query("SELECT * FROM user_answers ORDER BY id DESC")
    fun getAllAnswers(): Flow<List<UserAnswerEntity>>

    @Query("SELECT * FROM user_answers WHERE topicId = :topicId")
    suspend fun getAnswersByTopicSync(topicId: String): List<UserAnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: UserAnswerEntity)

    @Query("DELETE FROM user_answers")
    suspend fun clearAllAnswers()
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY id DESC")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE nextReviewTimeMills <= :currentTime ORDER BY nextReviewTimeMills ASC")
    fun getDueFlashcards(currentTime: Long): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteFlashcardById(id: Int)

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReviewTimeMills <= :currentTime")
    suspend fun getDueCount(currentTime: Long): Int
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarked_questions ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkQuestionEntity)

    @Query("DELETE FROM bookmarked_questions WHERE questionId = :questionId")
    suspend fun deleteBookmark(questionId: String)
}

@Database(
    entities = [
        UserEntity::class,
        SubjectEntity::class,
        TopicEntity::class,
        QuestionEntity::class,
        UserAnswerEntity::class,
        FlashcardEntity::class,
        BookmarkQuestionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun questionDao(): QuestionDao
    abstract fun userAnswerDao(): UserAnswerDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun bookmarkDao(): BookmarkDao
}

