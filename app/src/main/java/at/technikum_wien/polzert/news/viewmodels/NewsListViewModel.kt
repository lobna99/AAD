package at.technikum_wien.polzert.news.viewmodels

import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.*
import androidx.work.*
import at.technikum_wien.polzert.news.data.download.NewsDownloader
import at.technikum_wien.polzert.news.data.NewsItem
import at.technikum_wien.polzert.news.data.NewsRepository
import at.technikum_wien.polzert.news.data.db.ApplicationDatabase
import at.technikum_wien.polzert.news.settings.UserPreferencesRepository
import at.technikum_wien.polzert.news.workers.DeleteWorker
import at.technikum_wien.polzert.news.workers.DownloadWorker
import at.technikum_wien.polzert.news.workers.ImageDownloaderWorker
import at.technikum_wien.polzert.news.workers.PeriodicWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NewsListViewModel(
    private val newsRepository: NewsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    application: Application
) : AndroidViewModel(application) {
    private val _error = MutableLiveData(false)
    private val _busy = MutableLiveData(false)
    private var lastFeedUrl: String? = null
    lateinit var workManager: WorkManager


    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect {
                feedUrl.value = it.feedUrl
                showImages.value = it.showImages
                downloadImages.value = it.downloadImages
                if (it.downloadImages) {
                    val constraints = Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val workRequest = OneTimeWorkRequest.Builder(ImageDownloaderWorker::class.java)
                        .setConstraints(constraints)
                        .build()
                    WorkManager
                        .getInstance(getApplication<Application>().applicationContext)
                        .enqueue(workRequest)
                }
                if (newsRepository.newsItems.value?.isEmpty() == true) {
                    val data = Data.Builder()
                    data.apply {
                        putString("URL", feedUrl.value)
                    }
                    val constraints = Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val workRequest = OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                        .setConstraints(constraints)
                        .setInputData(data.build())
                        .build()
                    WorkManager
                        .getInstance(getApplication<Application>().applicationContext)
                        .enqueue(workRequest)
                }
                val data = Data.Builder()
                data.apply {
                    putString("URL", feedUrl.value)
                }
                val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val workRequest = PeriodicWorkRequest.Builder(
                    PeriodicWorker::class.java,
                    30,
                    TimeUnit.MINUTES
                ).setConstraints(constraints)
                    .setInputData(data.build())
                    .build()
                WorkManager
                    .getInstance(getApplication<Application>().applicationContext)
                    .enqueueUniquePeriodicWork(
                        DownloadWorker::class.java.canonicalName ?: "",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        workRequest
                    )
                if (lastFeedUrl != null && lastFeedUrl != it.feedUrl) {
                    val data = Data.Builder()
                    data.apply {
                        putString("URL", it.feedUrl)
                    }
                    val constraints = Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val workRequest = OneTimeWorkRequest.Builder(DeleteWorker::class.java)
                        .setConstraints(constraints)
                        .setInputData(data.build())
                        .build()
                    WorkManager
                        .getInstance(getApplication<Application>().applicationContext)
                        .enqueue(workRequest)
                }
                lastFeedUrl = it.feedUrl
            }
        }
    }

    val newsItems by lazy { newsRepository.newsItems }
    val error: LiveData<Boolean>
        get() = _error
    val busy: LiveData<Boolean>
        get() = _busy
    val feedUrl = MutableLiveData("")
    val showImages = MutableLiveData(false)
    val downloadImages = MutableLiveData(false)

    private fun downloadNewsItems(newsFeedUrl: String, delete: Boolean) {
        _error.value = false
        _busy.value = true
        viewModelScope.launch {
            if (delete)
                newsRepository.deleteAll()
            val newsItems = NewsDownloader().load(newsFeedUrl)
            when (newsItems) {
                null -> _error.value = true
                else -> newsRepository.updateOrInsertAll(newsItems)
            }
            _busy.value = false
        }
    }

    fun reload() {
        lastFeedUrl?.let {
            val data = Data.Builder()
            data.apply {
                putString("URL", lastFeedUrl)
            }
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                .setConstraints(constraints)
                .setInputData(data.build())
                .build()
            WorkManager
                .getInstance(getApplication<Application>().applicationContext)
                .enqueue(workRequest)
        }
    }

    fun updatePreferences(feedUrl: String, showImages: Boolean, downloadImages: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateFeedUrl(feedUrl = feedUrl)
            userPreferencesRepository.updateShowImages(showImages = showImages)
            userPreferencesRepository.updateDownloadImages(downloadImages = downloadImages)
        }
    }
}
