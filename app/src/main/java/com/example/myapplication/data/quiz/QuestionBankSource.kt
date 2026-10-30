package com.example.myapplication.data.quiz

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Supplies the raw bundled question bank (abstracted so the repository can be unit tested). */
interface QuestionBankSource {
    fun read(): String
}

@Singleton
class AssetQuestionBankSource @Inject constructor(
    @ApplicationContext private val context: Context
) : QuestionBankSource {
    override fun read(): String =
        context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }

    private companion object {
        const val ASSET_NAME = "questions.json"
    }
}
