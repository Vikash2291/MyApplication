package com.example.myapplication.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.database.dao.SettingsDao
import com.example.myapplication.data.local.database.dao.UserDao
import com.example.myapplication.data.local.database.entity.SettingsEntity
import com.example.myapplication.data.local.database.entity.UserEntity

@Database(
    entities = [UserEntity::class, SettingsEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun settingsDao(): SettingsDao
}
