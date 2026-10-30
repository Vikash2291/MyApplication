package com.example.myapplication.data.quiz

import com.example.myapplication.domain.quiz.MEGA_QUIZ_SIZE
import com.example.myapplication.data.local.database.dao.QuizDao
import com.example.myapplication.data.local.database.entity.AttemptAnswerEntity
import com.example.myapplication.data.local.database.entity.BookmarkEntity
import com.example.myapplication.data.local.database.entity.QuizAttemptEntity
import com.example.myapplication.data.local.database.entity.toDomain
import com.example.myapplication.data.local.database.entity.toEntity
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.domain.quiz.AnswerRecord
import com.example.myapplication.domain.quiz.AttemptReview
import com.example.myapplication.domain.quiz.CategoryProgress
import com.example.myapplication.domain.quiz.DailyQuizSelector
import com.example.myapplication.domain.quiz.Question
import com.example.myapplication.domain.quiz.QuizCategory
import com.example.myapplication.domain.quiz.QuizMode
import com.example.myapplication.domain.quiz.QuizRepository
import com.example.myapplication.domain.quiz.QuizScore
import com.example.myapplication.domain.quiz.QuizStats
import com.example.myapplication.domain.quiz.ReviewItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val dao: QuizDao,
    private val bankSource: QuestionBankSource,
    private val preferences: PreferencesRepository
) : QuizRepository {

    private val seedMutex = Mutex()
    @Volatile private var seeded = false

    override suspend fun ensureSeeded() {
        if (seeded) return
        seedMutex.withLock {
            if (seeded) return
            withContext(Dispatchers.IO) {
                val bank = QuestionBankParser.parse(bankSource.read())
                val storedVersion = preferences.getQuestionBankVersion()
                if (storedVersion < bank.version || dao.questionCount() < bank.questions.size) {
                    dao.upsertQuestions(bank.questions.map { it.toEntity() })
                    preferences.setQuestionBankVersion(bank.version)
                    Timber.d("Imported %d questions (bank v%d)", bank.questions.size, bank.version)
                }
            }
            seeded = true
        }
    }

    override suspend fun questionsFor(
        mode: QuizMode,
        category: QuizCategory?,
        epochDay: Long,
        count: Int
    ): List<Question> {
        ensureSeeded()
        val entities = when (mode) {
            QuizMode.CATEGORY -> {
                val cat = requireNotNull(category) { "Category quiz needs a category" }
                dao.questionsForCategory(cat.name, count)
            }
            QuizMode.DAILY -> {
                val ids = DailyQuizSelector.select(dao.allQuestionIds(), epochDay, count)
                val byId = dao.questionsByIds(ids).associateBy { it.id }
                ids.mapNotNull { byId[it] }
            }
            QuizMode.BOOKMARKS -> dao.randomBookmarkedQuestions(count)
            QuizMode.MEGA -> {
                val ids = dao.allQuestionIds().shuffled().take(MEGA_QUIZ_SIZE)
                val byId = dao.questionsByIds(ids).associateBy { it.id }
                ids.mapNotNull { byId[it] }
            }
        }
        return entities.mapNotNull { it.toDomain() }
    }

    override suspend fun saveAttempt(
        mode: QuizMode,
        category: QuizCategory?,
        epochDay: Long,
        answers: List<AnswerRecord>,
        score: QuizScore
    ): Long {
        val now = System.currentTimeMillis()
        val categoryOf = dao.questionsByIds(answers.map { it.questionId }).associate { it.id to it.category }
        return dao.recordAttempt(
            attempt = QuizAttemptEntity(
                mode = mode.name,
                category = category?.name,
                epochDay = epochDay,
                correctCount = score.correct,
                totalCount = score.total,
                points = score.points,
                completedAt = now
            ),
            answers = answers.mapIndexed { index, a ->
                AttemptAnswerEntity(
                    attemptId = 0,
                    position = index,
                    questionId = a.questionId,
                    selectedIndex = a.selectedIndex,
                    isCorrect = a.isCorrect
                )
            },
            categoryOf = categoryOf,
            now = now
        )
    }

    override suspend fun getAttemptReview(attemptId: Long): AttemptReview? {
        val attempt = dao.attemptById(attemptId) ?: return null
        val answers = dao.answersForAttempt(attemptId)
        val questions = dao.questionsByIds(answers.map { it.questionId })
            .mapNotNull { it.toDomain() }
            .associateBy { it.id }
        return AttemptReview(
            attemptId = attempt.id,
            mode = runCatching { QuizMode.valueOf(attempt.mode) }.getOrDefault(QuizMode.CATEGORY),
            category = QuizCategory.fromName(attempt.category),
            correct = attempt.correctCount,
            total = attempt.totalCount,
            points = attempt.points,
            items = answers.mapNotNull { a -> questions[a.questionId]?.let { ReviewItem(it, a.selectedIndex) } }
        )
    }

    override fun observeBookmarkIds(): Flow<Set<String>> = dao.observeBookmarkIds().map { it.toSet() }

    override fun observeBookmarkedQuestions(): Flow<List<Question>> =
        dao.observeBookmarkedQuestions().map { list -> list.mapNotNull { it.toDomain() } }

    override suspend fun setBookmarked(questionId: String, bookmarked: Boolean) {
        if (bookmarked) {
            dao.insertBookmark(BookmarkEntity(questionId, System.currentTimeMillis()))
        } else {
            dao.deleteBookmark(questionId)
        }
    }

    override fun observeStats(): Flow<QuizStats> = combine(
        dao.observeCategoryCoverage(),
        dao.observeCategoryProgress(),
        dao.observeAttemptCount()
    ) { coverage, progress, attempts ->
        val coverageBy = coverage.associateBy { it.category }
        val progressBy = progress.associateBy { it.category }
        val categories = QuizCategory.entries.map { cat ->
            val cov = coverageBy[cat.name]
            val p = progressBy[cat.name]
            CategoryProgress(
                category = cat,
                totalQuestions = cov?.totalQuestions ?: 0,
                seenQuestions = cov?.seenQuestions ?: 0,
                quizzesPlayed = p?.quizzesPlayed ?: 0,
                questionsAnswered = p?.questionsAnswered ?: 0,
                correctAnswers = p?.correctAnswers ?: 0,
                bestScore = p?.bestScore ?: 0
            )
        }
        QuizStats(
            quizzesPlayed = attempts,
            questionsAnswered = categories.sumOf { it.questionsAnswered },
            correctAnswers = categories.sumOf { it.correctAnswers },
            categories = categories
        )
    }

    override fun observeQuestionCounts(): Flow<Map<QuizCategory, Int>> =
        dao.observeCategoryCoverage().map { rows ->
            rows.mapNotNull { row -> QuizCategory.fromName(row.category)?.let { it to row.totalQuestions } }.toMap()
        }

    override suspend fun clearUserData() {
        dao.clearUserData()
    }
}
