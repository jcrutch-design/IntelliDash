package com.example.intellidash.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.intellidash.data.MediaDatabase
import com.example.intellidash.data.MediaItem
import com.example.intellidash.utils.RootStorageHelper

/**
 * Worker that perform a quick scan of the filesystem to discover media paths.
 * Runs immediately without constraints to populate the UI.
 */
class DiscoveryWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("DiscoveryWorker", "Starting immediate discovery scan")
        val database = MediaDatabase.getDatabase(applicationContext)
        val dao = database.mediaDao()

        return try {
            val files = RootStorageHelper.scanMediaFiles()
            Log.d("DiscoveryWorker", "Scanned ${files.size} files")
            
            var newItems = 0
            for (path in files) {
                if (!dao.exists(path)) {
                    dao.insert(MediaItem(path = path))
                    newItems++
                }
            }
            Log.d("DiscoveryWorker", "Inserted $newItems new items")
            Result.success()
        } catch (e: Exception) {
            Log.e("DiscoveryWorker", "Discovery failed", e)
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "DiscoveryWorker"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<DiscoveryWorker>()
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
