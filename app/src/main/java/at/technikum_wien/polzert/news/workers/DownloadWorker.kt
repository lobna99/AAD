package at.technikum_wien.polzert.news.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import at.technikum_wien.polzert.news.data.db.ApplicationDatabase
import at.technikum_wien.polzert.news.data.download.NewsDownloader
import at.technikum_wien.polzert.news.util.NotificationUtil
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class DownloadWorker(context: Context, params : WorkerParameters) :
    CoroutineWorker(context, params) {
        companion object {
            val LOG_TAG = DownloadWorker::class.java.simpleName
        }
        override suspend fun doWork(): Result {
            val url = inputData.getString("URL");
            val newsItems = NewsDownloader().load(url!!)

            ApplicationDatabase.getDatabase(applicationContext).newsItemDao().updateOrInsertAll(
                newsItems!!
            )

            return Result.success()
        }
}

