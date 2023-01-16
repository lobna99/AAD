package at.technikum_wien.polzert.news.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.technikum_wien.polzert.news.data.NewsRepository
import at.technikum_wien.polzert.news.settings.UserPreferencesRepository

class NewsListViewModelFactory(private val newsRepository : NewsRepository, private val userPreferencesRepository: UserPreferencesRepository,private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsListViewModel(newsRepository = newsRepository, userPreferencesRepository = userPreferencesRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
