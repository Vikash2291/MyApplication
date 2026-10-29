package com.example.myapplication.data.quiz

import com.example.myapplication.domain.quiz.QuizCategory
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/** Guards the bundled offline question bank (app/src/main/assets/questions.json). */
class QuestionBankTest {

    private val raw: String by lazy {
        // Unit tests run with the module directory as working directory.
        val file = listOf("src/main/assets/questions.json", "app/src/main/assets/questions.json")
            .map(::File)
            .first { it.exists() }
        file.readText(Charsets.UTF_8)
    }

    private val bank by lazy { QuestionBankParser.parse(raw) }

    @Test
    fun `bank has at least 300 valid questions and none were dropped`() {
        val declared = Regex("\"id\"\\s*:").findAll(raw).count()
        assertThat(bank.questions.size).isAtLeast(300)
        assertThat(bank.questions.size).isEqualTo(declared)
        assertThat(bank.version).isAtLeast(1)
    }

    @Test
    fun `every category is well covered`() {
        val byCategory = bank.questions.groupingBy { it.category }.eachCount()
        QuizCategory.entries.forEach { category ->
            assertThat(byCategory[category] ?: 0).isAtLeast(30)
        }
    }

    @Test
    fun `ids are unique and questions are well formed in both languages`() {
        assertThat(bank.questions.map { it.id }.toSet()).hasSize(bank.questions.size)
        val devanagari = Regex("[\\u0900-\\u097F]")
        bank.questions.forEach { q ->
            assertThat(q.answerIndex).isIn(0..3)
            listOf(q.english, q.hindi).forEach { l ->
                assertThat(l.text).isNotEmpty()
                assertThat(l.explanation).isNotEmpty()
                assertThat(l.options).hasSize(4)
                assertThat(l.options.toSet()).hasSize(4)
            }
            assertThat(devanagari.containsMatchIn(q.hindi.text)).isTrue()
        }
    }

    @Test
    fun `parser skips malformed entries`() {
        val json = """
            {"version": 3, "questions": [
              {"id": "ok", "category": "SCIENCE", "answer": 1,
               "en": {"q": "Q", "options": ["a","b","c","d"], "explanation": "e"},
               "hi": {"q": "प्र", "options": ["क","ख","ग","घ"], "explanation": "व"}},
              {"id": "bad-cat", "category": "NOPE", "answer": 0,
               "en": {"q": "Q", "options": ["a","b","c","d"], "explanation": "e"},
               "hi": {"q": "प्र", "options": ["क","ख","ग","घ"], "explanation": "व"}},
              {"id": "bad-answer", "category": "SCIENCE", "answer": 4,
               "en": {"q": "Q", "options": ["a","b","c","d"], "explanation": "e"},
               "hi": {"q": "प्र", "options": ["क","ख","ग","घ"], "explanation": "व"}},
              {"id": "three-options", "category": "SCIENCE", "answer": 0,
               "en": {"q": "Q", "options": ["a","b","c"], "explanation": "e"},
               "hi": {"q": "प्र", "options": ["क","ख","ग"], "explanation": "व"}}
            ]}
        """.trimIndent()
        val parsed = QuestionBankParser.parse(json)
        assertThat(parsed.version).isEqualTo(3)
        assertThat(parsed.questions.map { it.id }).containsExactly("ok")
        assertThat(parsed.questions.single().category).isEqualTo(QuizCategory.SCIENCE)
    }
}
