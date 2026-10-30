package com.example.myapplication.utils

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.myapplication.core.common.Constants
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.domain.model.Result as DomainResult
import com.example.myapplication.domain.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/** Periodically refreshes the signed-in user's profile and token in the background. */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val userRepository: UserRepository,
    private val preferencesRepository: PreferencesRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return when (val result = userRepository.refreshUserProfile()) {
            is DomainResult.Success -> {
                preferencesRepository.updateLastSyncTime()
                Result.success()
            }
            is DomainResult.Error -> {
                Timber.w("Sync failed: %s", result.message)
                if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
            }
        }
    }

    companion object {
        private const val MAX_RETRIES = 3

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                Constants.SYNC_INTERVAL_HOURS, TimeUnit.HOURS
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                Constants.SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
