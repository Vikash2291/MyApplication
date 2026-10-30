package com.example.myapplication.data.remote

import com.example.myapplication.domain.model.User
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Optional backend used to mirror the Firebase user. Every call is best-effort:
 * the app keeps working with Firebase + Room when the backend is unreachable.
 */
interface ApiService {

    @POST("auth/sync")
    suspend fun syncSession(@Body request: SessionSyncRequest): Response<UserProfileResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("user/profile")
    suspend fun getProfile(): Response<UserProfileResponse>

    @PUT("user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserProfileResponse>

    @DELETE("user")
    suspend fun deleteAccount(): Response<Unit>
}

@Serializable
data class SessionSyncRequest(
    val user: User,
    val deviceId: String,
    val deviceName: String
)

@Serializable
data class UserProfileResponse(
    val user: User
)

@Serializable
data class UpdateProfileRequest(
    val displayName: String?,
    val photoUrl: String?
)
