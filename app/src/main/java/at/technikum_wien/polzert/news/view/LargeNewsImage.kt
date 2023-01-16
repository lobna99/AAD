package at.technikum_wien.polzert.news.view

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import at.technikum_wien.polzert.news.data.NewsItem
import at.technikum_wien.polzert.news.ui.theme.SemiTransparentWhite
import at.technikum_wien.polzert.news.util.Util
import at.technikum_wien.polzert.news.viewmodels.NewsListViewModel
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage
import java.io.File

@Composable
fun LargeNewsImage(viewModel : NewsListViewModel, newsItem : NewsItem) {
    val context = LocalContext.current
    val showImages by viewModel.showImages.observeAsState()
    val downloadImages by viewModel.downloadImages.observeAsState()

    Box(contentAlignment = Alignment.BottomStart) {
        if (newsItem.imageUrl != null && showImages != false) {
            if(downloadImages == true){
                val sd: File = context.cacheDir
                val folder = File(sd, "/images/")
                val fileName = File(folder, newsItem.identifier+".jpg")
                val uri = Uri.fromFile(fileName)
                GlideImage(
                    imageModel = uri,
                    contentScale = ContentScale.Fit,
                    circularReveal = CircularReveal(duration = 250),
                    modifier = Modifier.fillMaxWidth()
                )
            }else {
                GlideImage(
                    imageModel = newsItem.imageUrl!!,
                    contentScale = ContentScale.Fit,
                    circularReveal = CircularReveal(duration = 250),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Row(modifier = Modifier.padding(start = 50.dp, bottom = 5.dp, end = 5.dp)) {
            Column(
                modifier = Modifier
                    .background(
                        color = SemiTransparentWhite
                    )
            ) {
                Text(
                    text = newsItem.title,
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 5.dp)
                )
                Text(
                    text = newsItem.author ?: "",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 5.dp)
                )
                Text(
                    text = Util.instantToString(newsItem.publicationDate),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 5.dp)
                )
            }
        }
    }
}
