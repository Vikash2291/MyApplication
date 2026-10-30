package com.example.myapplication.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Re-schedules background sync after a reboot or app update. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> SyncWorker.schedule(context)
        }
    }
}
