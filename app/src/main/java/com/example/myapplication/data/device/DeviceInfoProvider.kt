package com.example.myapplication.data.device

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface DeviceInfoProvider {
    val deviceId: String
    val deviceName: String
}

@Singleton
class AndroidDeviceInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceInfoProvider {
    @get:SuppressLint("HardwareIds")
    override val deviceId: String
        get() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown"

    override val deviceName: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"
}
