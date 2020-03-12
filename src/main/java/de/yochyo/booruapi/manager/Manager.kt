package de.yochyo.booruapi.manager

import de.yochyo.booruapi.api.Api
import de.yochyo.booruapi.objects.Post
import de.yochyo.eventcollection.EventCollection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Manager(val api: Api, val tags: Array<String>, override val limit: Int) : IManager {
    private val mutex = Mutex()

    override val posts = EventCollection<Post>(ArrayList())


    var currentPage = 1
        private set

    override suspend fun downloadNextPage(): List<Post>? {
        return downloadNextPages(1)
    }

    override suspend fun downloadNextPages(amount: Int): List<Post>? {
        return mutex.withLock {
            val result: MutableList<Post> = ArrayList(limit * amount)

            val pages = (currentPage until currentPage + amount).map {
                GlobalScope.async { api.getPosts(it, tags, limit) }
            }.awaitAll()

            var downloadedPages = 0
            for (page in pages) {
                if (page == null) break
                page.addFilteredPosts(result)
                downloadedPages++
            }

            if (downloadedPages == 0) {
                null
            } else {
                posts += result
                currentPage += downloadedPages
                result
            }
        }
    }

    private fun List<Post>.addFilteredPosts(copyTo: MutableList<Post>){
        val lastPostID = if (copyTo.isNotEmpty()) copyTo.last().id else Integer.MAX_VALUE
        copyTo += this.takeLastWhile { lastPostID > it.id }
    }

    override suspend fun clear() {
        mutex.withLock {
            posts.clear()
            currentPage = 1
        }
    }

    override fun toString(): String = tags.joinToString(" ") { it }
}