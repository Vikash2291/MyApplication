package com.example.myapplication.data.quiz

import com.example.myapplication.domain.quiz.LocalizedQuestion
import com.example.myapplication.domain.quiz.Question
import com.example.myapplication.domain.quiz.QuizCategory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class QuestionBankDto(
    val version: Int,
    val questions: List<QuestionDto>
)

@Serializable
data class QuestionDto(
    val id: String,
    val category: String,
    val answer: Int,
    val en: LocalizedDto,
    val hi: LocalizedDto
)

@Serializable
data class LocalizedDto(
    val q: String,
    val options: List<String>,
    val explanation: String
)

data class QuestionBank(val version: Int, val questions: List<Question>)

/** Parses assets/questions.json. Malformed entries are skipped rather than crashing the app. */
object QuestionBankParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(raw: String): QuestionBank {
        val dto = json.decodeFromString(QuestionBankDto.serializer(), raw)
        val questions = dto.questions.mapNotNull { it.toDomainOrNull() }
        return QuestionBank(dto.version, questions)
    }

    private fun QuestionDto.toDomainOrNull(): Question? {
        val cat = QuizCategory.fromName(category) ?: return null
        if (en.options.size != OPTION_COUNT || hi.options.size != OPTION_COUNT) return null
        if (answer !in 0 until OPTION_COUNT) return null
        return Question(
            id = id,
            category = cat,
            answerIndex = answer,
            english = LocalizedQuestion(en.q, en.options, en.explanation),
            hindi = LocalizedQuestion(hi.q, hi.options, hi.explanation)
        )
    }

    const val OPTION_COUNT = 4
}
