package de.yochyo.booruapi.manager

import de.yochyo.booruapi.objects.Post
import de.yochyo.eventcollection.EventCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class ManagerFolder(val managers: Collection<IManager>, override val limit: Int) : IManager {
    private val mutex = Mutex()

    override val posts: EventCollection<Post> = EventCollection(ArrayList())

    private val bufferedPosts = TreeSet<Post>()

    /**
     * @return empty ist on end or error
     */
    override suspend fun downloadNextPage(): List<Post>? {
        return downloadNextPages(1)
    }

    override suspend fun downloadNextPages(amount: Int): List<Post>? {
        return mutex.withLock {
            while (hasToDownload(limit*amount)) {
                var allPagesEmpty = true

                withContext(Dispatchers.IO) {
                    //waits for all subroutines
                    managers.filter { hasToDownload(it, limit*amount) }.forEach {
                        launch {
                            val page = it.downloadNextPage()
                            if (page != null && page.isNotEmpty()) {
                                allPagesEmpty = false
                                bufferedPosts += page
                            }
                        }
                    }
                }
                if (allPagesEmpty) break
            }
            val r = takeLast(limit)
            posts += r
            r
        }
    }

    private fun hasToDownload(limit: Int): Boolean {
        if (bufferedPosts.size < limit) return true
        val highest = managers.map { if (it.posts.isEmpty()) 0 else it.posts.last().id }.max()!!
        val downloadIfHigher = bufferedPosts.elementAt(bufferedPosts.size - limit).id
        return highest > downloadIfHigher
    }

    private fun hasToDownload(m: IManager, limit: Int): Boolean {
        if (bufferedPosts.size < limit) return true
        val highest = if (m.posts.isEmpty()) 0 else m.posts.last().id
        val downloadIfHigher = bufferedPosts.elementAt(bufferedPosts.size - limit).id
        return highest > downloadIfHigher
    }

    private fun takeLast(n: Int): List<Post> {
        val result = ArrayList<Post>(n)
        for (i in 0 until n) {
            val last = bufferedPosts.pollLast()
            if (last != null) result += last
        }
        return result
    }

    override suspend fun clear() {
        mutex.withLock {
            for (m in managers) m.clear()
            posts.clear()
            bufferedPosts.clear()
        }
    }

    override fun toString(): String {
        return managers.joinToString(" OR ") { it.toString() }
    }
}