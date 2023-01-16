package at.technikum_wien.polzert.news.data.db

import android.icu.number.IntegerWidth
import at.technikum_wien.polzert.news.data.NewsItem

import androidx.lifecycle.LiveData
import androidx.room.*
import java.time.Instant

@Dao
abstract class NewsItemDao {

    var newestitems : List<NewsItem> = ArrayList()



    @get:Query("SELECT * FROM news_item ORDER BY publication_date DESC")
    abstract val newsItems: LiveData<List<NewsItem>>
    @get:Query("SELECT * FROM news_item ORDER BY publication_date DESC")
    abstract val newsItemsList: List<NewsItem>

    @Query("SELECT _id FROM news_item WHERE identifier = :identifier")
    abstract suspend fun getIdForIdentifier(identifier: String): Long

    @Query("SELECT _id FROM news_item WHERE publication_date <=:date")
    abstract suspend fun getPublicationDate(date: Int): List<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insert(newsItem: NewsItem): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insert(newsItems: List<NewsItem>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun tryInsert(newsItem: NewsItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun tryInsert(newsItems: List<NewsItem>): List<Long>

    @Update
    abstract suspend fun update(newsItem: NewsItem)

    @Update
    abstract suspend fun update(newsItems: List<NewsItem>)

    @Delete
    abstract suspend fun delete(newsItem: NewsItem)

    @Query("DELETE FROM news_item WHERE _id=:id")
    abstract suspend fun deleteOldItems(id: Long)

    @Query("DELETE FROM news_item")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun updateOrInsertAll(newsItems: List<NewsItem>) {
        val insertResult: List<Long> = tryInsert(newsItems)
        val newItem: MutableList<NewsItem> = ArrayList()
        val updateList: MutableList<NewsItem> = ArrayList()
        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                newsItems[i].id = getIdForIdentifier(newsItems[i].identifier)
                updateList.add(newsItems[i])
            }else {
                newItem.add(newsItems[i])
            }
        }
        if (updateList.isNotEmpty()) {
            update(updateList)
        }
        if(newItem.isNotEmpty()){
            setNewestItem(newItem)
        }
    }

    @Transaction
    open suspend fun replaceAll(newsItems: List<NewsItem>) {
        deleteAll()
        insert(newsItems)
    }

    fun setNewestItem(newsItem: MutableList<NewsItem>) {
        newestitems = newsItem
    }
}
