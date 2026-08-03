package com.example.myapplication.data.remote

import com.example.myapplication.domain.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("user/profile")
    suspend fun getProfile(): Response<UserProfileResponse>

    @POST("user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserProfileResponse>

    @POST("user/delete")
    suspend fun deleteAccount(): Response<Unit>

    @POST("password/reset")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequest): Response<Unit>
}

data class LoginRequest(
    val email: String,
    val password: String,
    val deviceId: String,
    val deviceName: String
)

data class LoginResponse(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val deviceId: String,
    val deviceName: String
)

data class RegisterResponse(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class UserProfileResponse(
    val user: User
)

data class UpdateProfileRequest(
    val displayName: String?,
    val photoUrl: String?
)

data class PasswordResetRequest(
    val email: String
)