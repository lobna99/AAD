package at.technikum_wien.polzert.news.workers

import android.R.attr.bitmap
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.technikum_wien.polzert.news.data.db.ApplicationDatabase
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.String


class ImageDownloaderWorker(context: Context, params : WorkerParameters) :
    CoroutineWorker(context, params) {
    companion object {
        val LOG_TAG = ImageDownloaderWorker::class.java.simpleName
    }
    override suspend fun doWork(): Result {
        val items = ApplicationDatabase.getDatabase(applicationContext).newsItemDao().newsItemsList
        for(i in items) {
            val sd: File = applicationContext.cacheDir
            val folder = File(sd, "/images/")
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    Log.e("ERROR", "Cannot create a directory!")
                } else {
                    folder.mkdirs()
                }
            }

            val fileName = File(folder, i.identifier)

            try {
                val outputStream = FileOutputStream(String.valueOf(fileName)+".jpg")
                Glide.with(applicationContext)
                    .asBitmap()
                    .load(i.imageUrl)
                    .submit().get().compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return Result.success()
    }
}