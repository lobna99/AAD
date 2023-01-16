package at.technikum_wien.polzert.news.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.util.LruCache
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import at.technikum_wien.polzert.news.data.NewsRepository
import at.technikum_wien.polzert.news.settings.UserPreferencesRepository
import at.technikum_wien.polzert.news.settings.dataStore
import at.technikum_wien.polzert.news.ui.theme.NewsTheme
import at.technikum_wien.polzert.news.util.NotificationUtil
import at.technikum_wien.polzert.news.view.DetailScreen
import at.technikum_wien.polzert.news.view.MainScreen
import at.technikum_wien.polzert.news.view.Navigation
import at.technikum_wien.polzert.news.view.Screen
import at.technikum_wien.polzert.news.viewmodels.NewsListViewModel
import at.technikum_wien.polzert.news.viewmodels.NewsListViewModelFactory
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
class MainActivity : ComponentActivity() {
    private lateinit var memoryCache: LruCache<String, Bitmap>
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        NotificationUtil.registerNotificationChannel(this)
        val viewModel = ViewModelProvider(this, NewsListViewModelFactory(NewsRepository(applicationContext), UserPreferencesRepository(dataStore),application))[NewsListViewModel::class.java]

        setContent {
            NewsTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Navigation(viewModel = viewModel)
                }
            }
        }
    }

}
