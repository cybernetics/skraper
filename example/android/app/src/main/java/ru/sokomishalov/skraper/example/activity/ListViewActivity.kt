package ru.sokomishalov.skraper.example.activity

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import ru.sokomishalov.skraper.SkraperClient
import ru.sokomishalov.skraper.client.okhttp3.OkHttp3SkraperClient
import ru.sokomishalov.skraper.example.R
import ru.sokomishalov.skraper.example.adapter.ListViewModelAdapter
import ru.sokomishalov.skraper.provider.facebook.FacebookSkraper
import ru.sokomishalov.skraper.provider.facebook.getUserPosts
import ru.sokomishalov.skraper.provider.instagram.InstagramSkraper
import ru.sokomishalov.skraper.provider.instagram.getUserPosts
import ru.sokomishalov.skraper.provider.ninegag.NinegagSkraper
import ru.sokomishalov.skraper.provider.ninegag.getTagPosts
import ru.sokomishalov.skraper.provider.twitter.TwitterSkraper
import ru.sokomishalov.skraper.provider.twitter.getUserPosts
import kotlin.coroutines.CoroutineContext

class ListViewActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext get() = job + Dispatchers.Main

    private val job: Job = Job()
    private val listViewAdapter: ListViewModelAdapter = ListViewModelAdapter(context = this, data = mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_view)
        fetchItems()
        val listView = findViewById<ListView>(R.id.postsListView)
        listView.adapter = listViewAdapter
    }

    private fun fetchItems() = launch {
        val items = withContext(Dispatchers.Default) {
            listOf(
                NinegagSkraper(client = DEFAULT_CLIENT).getTagPosts(tag = "meme", limit = DEFAULT_LIMIT),
                TwitterSkraper(client = DEFAULT_CLIENT).getUserPosts(username = "memes", limit = DEFAULT_LIMIT),
                FacebookSkraper(client = DEFAULT_CLIENT).getUserPosts(username = "memes", limit = DEFAULT_LIMIT),
                InstagramSkraper(client = DEFAULT_CLIENT).getUserPosts(username = "memes", limit = DEFAULT_LIMIT)
            ).flatten()
        }
        listViewAdapter + items
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        private const val DEFAULT_LIMIT: Int = 5
        private val DEFAULT_CLIENT: SkraperClient = OkHttp3SkraperClient()
    }
}

