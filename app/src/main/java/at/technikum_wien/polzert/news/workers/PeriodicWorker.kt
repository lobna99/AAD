package at.technikum_wien.polzert.news.workers

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.technikum_wien.polzert.news.data.db.ApplicationDatabase
import at.technikum_wien.polzert.news.data.download.NewsDownloader
import at.technikum_wien.polzert.news.util.NotificationUtil
import java.time.Instant
import java.util.*

class PeriodicWorker(context: Context, params : WorkerParameters) :
    CoroutineWorker(context, params) {
    companion object {
        val LOG_TAG = PeriodicWorker::class.java.simpleName
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val database = ApplicationDatabase.getDatabase(applicationContext)
        val url = inputData.getString("URL");
        val newsItems = NewsDownloader().load(url!!)
        val c1 = Calendar.getInstance()
        c1.add(Calendar.DATE,-5)
        val dateOne = c1.time
        val inst: Instant = dateOne.toInstant()
        val dt : Int = (inst.epochSecond/1000).toInt()
        val ids = database.newsItemDao().getPublicationDate(dt)
        for (id in ids)
            database.newsItemDao().deleteOldItems(id)
        database.newsItemDao().updateOrInsertAll(
            newsItems!!
        )
        val new = database.newsItemDao().newestitems
        for (i in new)
            NotificationUtil.createNotification(applicationContext,i)

        return Result.success()
    }
}