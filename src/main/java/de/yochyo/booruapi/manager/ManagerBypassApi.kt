package de.yochyo.booruapi.manager

import de.yochyo.booruapi.api.Api
import de.yochyo.booruapi.objects.Post
import de.yochyo.eventcollection.EventCollection
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ManagerBypassApi(val api: Api, private val tags: Collection<String>, override val limit: Int, defaultApiLimit: Int = 2) : IManager {
    private val mutex = Mutex()
    private val manager = Manager(api, tags.take(defaultApiLimit).toTypedArray(), limit)
    override val posts: EventCollection<Post> = EventCollection(ArrayList())

    private val t = tags.drop(defaultApiLimit)

    /**
     * @return returns empty ist on end and null on error or if all elements were filtered
     */
    override suspend fun downloadNextPages(amount: Int): List<Post>? {
        return mutex.withLock {
            var pages = manager.downloadNextPages(amount)
            if (pages != null) {
                val isNotEmpty = pages.isNotEmpty()
                pages = pages.filter {
                    for (tag in t) if (it.tagString.contains(" $tag ")) return@filter false
                    true
                }
                if (isNotEmpty && pages.isEmpty()) pages = null
                else posts += pages
            }
            pages
        }
    }

    override suspend fun downloadNextPage(): List<Post>? {
        return downloadNextPages(1)
    }

    override suspend fun clear() {
        posts.clear()
    }

    override fun toString(): String {
        return tags.joinToString(" AND ") { it }
    }
}