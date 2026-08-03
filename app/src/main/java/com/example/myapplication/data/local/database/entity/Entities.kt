package com.example.myapplication.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "users")
@Serializable
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
    val createdAt: Long,
    val lastLoginAt: Long,
    val accessToken: String?,
    val refreshToken: String?,
    val tokenExpiresAt: Long
)

@Entity(tableName = "settings")
@Serializable
data class SettingsEntity(
    @PrimaryKey
    val key: String,
    val value: String
)