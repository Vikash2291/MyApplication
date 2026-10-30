package com.example.myapplication.data.remote

import android.os.Build
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.local.preferences.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/** Adds standard headers and the bearer token (Firebase ID token) to every request. */
@Singleton
class AuthInterceptor @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // OkHttp interceptors run on a background thread, so blocking here is safe.
        val token = runBlocking { preferencesRepository.authToken.first() }

        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .header(
                "User-Agent",
                "MyApplication/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.SDK_INT})"
            )
            .apply { if (!token.isNullOrEmpty()) header("Authorization", "Bearer $token") }
            .build()

        return chain.proceed(request)
    }
}
