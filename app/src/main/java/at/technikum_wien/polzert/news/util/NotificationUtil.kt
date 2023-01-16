package at.technikum_wien.polzert.news.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import at.technikum_wien.polzert.news.R
import at.technikum_wien.polzert.news.data.NewsItem
import com.bumptech.glide.Glide
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage

object NotificationUtil {
    private val LOG_TAG = NotificationUtil::class.java.simpleName
    private const val NOTIFICATION_ID_KEY = "notification_id"
    const val CHANNEL_ID = "my_channel_01"

    fun registerNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "My Channel 01"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }
    }

    fun createNotification(context: Context, newsItem: NewsItem) {
        val sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        var notificationId = sharedPreferences.getInt(NOTIFICATION_ID_KEY, 0)
        notificationId++
        sharedPreferences.edit().putInt(NOTIFICATION_ID_KEY, notificationId).apply()
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(Glide.with(context)
                .asBitmap()
                .load(newsItem.imageUrl)
                .submit().get())
            .setContentTitle(newsItem.title)
            .setAutoCancel(true)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

        val intent = Intent(Intent.ACTION_VIEW, "mydata://show?data=${newsItem.identifier}".toUri())
        val taskStackBuilder = TaskStackBuilder.create(context)
        taskStackBuilder.addNextIntentWithParentStack(intent)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            taskStackBuilder.getPendingIntent(
                notificationId,
                PendingIntent.FLAG_IMMUTABLE.or((PendingIntent.FLAG_UPDATE_CURRENT))
            )
        } else {
            taskStackBuilder.getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        notificationBuilder.setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}