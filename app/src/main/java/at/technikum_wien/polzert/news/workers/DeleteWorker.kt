package at.technikum_wien.polzert.news.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import at.technikum_wien.polzert.news.data.db.ApplicationDatabase
import at.technikum_wien.polzert.news.data.download.NewsDownloader
import at.technikum_wien.polzert.news.util.NotificationUtil

class DeleteWorker(context: Context, params : WorkerParameters) :
    CoroutineWorker(context, params) {
    companion object {
        val LOG_TAG = DeleteWorker::class.java.simpleName
    }
    override suspend fun doWork(): Result {
        ApplicationDatabase.getDatabase(applicationContext).newsItemDao().deleteAll()
        val url = inputData.getString("URL");
        val newsItems = NewsDownloader().load(url!!)
        ApplicationDatabase.getDatabase(applicationContext).newsItemDao().updateOrInsertAll(
            newsItems!!
        )
        val new = ApplicationDatabase.getDatabase(applicationContext).newsItemDao().newestitems
        for (i in new)
            NotificationUtil.createNotification(applicationContext,i)
        return Result.success()
    }
}