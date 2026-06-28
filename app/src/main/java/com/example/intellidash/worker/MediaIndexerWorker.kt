package com.example.intellidash.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.intellidash.MultimodalIndexerClient
import com.example.intellidash.data.MediaDatabase
import com.example.intellidash.data.MediaItem
import com.example.intellidash.utils.RootStorageHelper
import java.util.concurrent.TimeUnit

/**
 * Worker that periodically indexes media files using root access and multimodal AI.
 */
class MediaIndexerWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("MediaIndexerWorker", "Starting media indexing work")
        val database = MediaDatabase.getDatabase(applicationContext)
        val dao = database.mediaDao()
        val indexerClient = MultimodalIndexerClient(applicationContext)

        try {
            // 1. Scan for new files (Root access)
            val files = RootStorageHelper.scanMediaFiles()
            Log.d("MediaIndexerWorker", "Scanned ${files.size} files")
            for (path in files) {
                if (!dao.exists(path)) {
                    dao.insert(MediaItem(path = path))
                }
            }

            // 2. Index items in a loop (Batch size for responsiveness)
            var indexedCount = 0
            val maxBatchSize = 10
            
            while (indexedCount < maxBatchSize) {
                val item = dao.getNextUnindexedItem() ?: break
                
                Log.d("MediaIndexerWorker", "Indexing ($indexedCount/$maxBatchSize): ${item.path}")

                // 3. Perform AI inference
                val bitmap = RootStorageHelper.readMediaFile(item.path)
                if (bitmap == null) {
                    Log.e("MediaIndexerWorker", "Failed to read bitmap for ${item.path}")
                    dao.update(item.copy(summary = "Error: IO Failure", indexedAt = System.currentTimeMillis()))
                    indexedCount++
                    continue
                }

                val response = indexerClient.indexImage(bitmap)
                bitmap.recycle()

                if (response != null) {
                    // 4. Update the DB with metadata
                    dao.update(
                        item.copy(
                            summary = response.summary,
                            category = response.category,
                            tags = response.tags,
                            indexedAt = System.currentTimeMillis()
                        )
                    )
                    Log.d("MediaIndexerWorker", "Successfully indexed ${item.path}")
                    indexedCount++
                } else {
                    Log.w("MediaIndexerWorker", "AI inference failed for ${item.path}")
                    // If one fails, we might want to retry later, but for now let's just move to next or stop batch
                    break 
                }
            }
            
            return if (dao.getNextUnindexedItem() != null) {
                // If there are more items, we could return retry to trigger another run later
                // but for one-time work REPLACE policy is used, so it might not be needed.
                // However, for periodic work, it will run again in an hour.
                Result.success()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("MediaIndexerWorker", "Worker failed", e)
            return Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "MediaIndexerWorker"
        private const val ONE_TIME_WORK_NAME = "MediaIndexerWorker_OneTime"

        /**
         * Enqueues periodic indexing work with charging and idle constraints.
         */
        fun enqueuePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiresDeviceIdle(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<MediaIndexerWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        /**
         * Enqueues a one-time indexing job to start immediately.
         * This is used when the user opens the app to ensure fresh results.
         */
        fun enqueueOneTimeWork(context: Context) {
            val request = OneTimeWorkRequestBuilder<MediaIndexerWorker>()
                .addTag(ONE_TIME_WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
