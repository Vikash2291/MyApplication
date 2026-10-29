package com.example.myapplication.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
    val createdAt: Long,
    val lastLoginAt: Long,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val tokenExpiresAt: Long = 0L
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)

fun UserEntity.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl,
    isEmailVerified = isEmailVerified,
    createdAt = createdAt,
    lastLoginAt = lastLoginAt
)

fun User.toEntity(
    accessToken: String? = null,
    refreshToken: String? = null,
    tokenExpiresAt: Long = 0L
): UserEntity = UserEntity(
    id = id,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl,
    isEmailVerified = isEmailVerified,
    createdAt = createdAt,
    lastLoginAt = lastLoginAt,
    accessToken = accessToken,
    refreshToken = refreshToken,
    tokenExpiresAt = tokenExpiresAt
)
