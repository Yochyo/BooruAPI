package de.yochyo.booruapi.manager

import de.yochyo.booruapi.api.IBooruApi
import de.yochyo.booruapi.api.Post
import de.yochyo.booruapi.utils.removeDuplicatesUpdateCachedList
import de.yochyo.eventcollection.EventCollection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.collections.ArrayList

class Manager(val api: IBooruApi, val tags: String, override val limit: Int) : IManager {
    private val mutex = Mutex()

    override val posts = EventCollection<Post>(ArrayList())
    private val cachedPostIds = TreeSet<Int>()


    var currentPage = 1
        private set

    /**
     * @return empty list on end null on error
     */
    override suspend fun downloadNextPage(): List<Post>? {
        return downloadNextPages(1)
    }

    override suspend fun downloadNextPages(amount: Int): List<Post>? {
        return mutex.withLock {
            val pages = (currentPage until currentPage + amount).map {
                GlobalScope.async { api.getPosts(it, tags, limit) }
            }.awaitAll()

            //If an error occured while downloading a page, all pages after the error are scrapped by only taking the pages before
            val pagesUntilError = pages.takeWhile { it != null }.mapNotNull { it }
            if (pagesUntilError.isEmpty()) return null

            currentPage += pagesUntilError.size

            val allPagesAsList = LinkedList<Post>().apply { pagesUntilError.forEach { this += it } }
            val resultWithoutDuplicates = removeDuplicatesUpdateCachedList(cachedPostIds, allPagesAsList)
            posts += resultWithoutDuplicates
            resultWithoutDuplicates
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            posts.clear()
            cachedPostIds.clear()
            currentPage = 1
        }
    }

    override fun toString(): String = tags
}