package com.example.myapplication.data.remote

import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkModule @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {

    private const val BASE_URL = "https://api.example.com/"
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    fun createApiService(): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(preferencesRepository))
            .retryOnConnectionFailure(true)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(asConverterFactory(json))
            .build()
            .create(ApiService::class.java)
    )
    }
}

class AuthInterceptor @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()

        // Add auth token if available
        val authToken = getAuthToken()

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("User-Agent", "MyApplication/${BuildConfig.VERSION_NAME} (Android ${android.os.Build.VERSION.SDK_INT})")

        if (authToken != null && authToken.isNotEmpty()) {
            requestBuilder.header("Authorization", "Bearer $authToken")
        }

        return chain.proceed(requestBuilder.build())
    }

    private suspend fun getAuthToken(): String? {
        // Get token from preferences
        return try {
            preferencesRepository.userId.first()
        } catch (e: Exception) {
            null
        }
    }
}