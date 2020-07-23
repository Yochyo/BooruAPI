package de.yochyo.booruapi.manager

import de.yochyo.booruapi.objects.Post
import de.yochyo.eventcollection.EventCollection
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max

class BufferedManager(private val manager: IManager) : IManager by manager {
    private val utils = ManagerUtils()
    override val posts: EventCollection<Post> = EventCollection(ArrayList())

    private val bufferedPosts = LinkedList<Post>()
    private val mutex = Mutex()

    private var lastDownloadsForFullPage = 1
    override suspend fun downloadNextPages(amount: Int): List<Post>? {
        return mutex.withLock {
            var curDownloadsForFullPage = 0
            while (bufferedPosts.size < limit * amount) {
                val page = manager.downloadNextPages(lastDownloadsForFullPage * amount)
                if (page == null) return@withLock null
                else if (page.isEmpty()) break
                utils.addPageTo(bufferedPosts, page)
                curDownloadsForFullPage++
            }
            lastDownloadsForFullPage = max(1, curDownloadsForFullPage)
            val result = take(limit*amount)
            posts += result
            return result
        }
    }

    override suspend fun downloadNextPage(): List<Post>? {
        return downloadNextPages(1)
    }

    private fun take(n: Int): List<Post> {
        val result = LinkedList<Post>()
        try {
            for (i in 0 until n) result += bufferedPosts.removeFirst()
        } catch (e: java.lang.Exception) {
        }
        return result
    }

    override suspend fun clear() {
        mutex.withLock {
            posts.clear()
            manager.clear()
            bufferedPosts.clear()
        }
    }

    override fun toString(): String {
        return manager.toString()
    }
}