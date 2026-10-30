package com.example.myapplication.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.local.database.dao.QuizDao
import com.example.myapplication.data.local.database.dao.SettingsDao
import com.example.myapplication.data.local.database.dao.UserDao
import com.example.myapplication.data.local.database.entity.AttemptAnswerEntity
import com.example.myapplication.data.local.database.entity.BookmarkEntity
import com.example.myapplication.data.local.database.entity.CategoryProgressEntity
import com.example.myapplication.data.local.database.entity.QuestionEntity
import com.example.myapplication.data.local.database.entity.QuestionProgressEntity
import com.example.myapplication.data.local.database.entity.QuizAttemptEntity
import com.example.myapplication.data.local.database.entity.SettingsEntity
import com.example.myapplication.data.local.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        SettingsEntity::class,
        QuestionEntity::class,
        QuestionProgressEntity::class,
        QuizAttemptEntity::class,
        AttemptAnswerEntity::class,
        CategoryProgressEntity::class,
        BookmarkEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(QuizConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun settingsDao(): SettingsDao
    abstract fun quizDao(): QuizDao
}
