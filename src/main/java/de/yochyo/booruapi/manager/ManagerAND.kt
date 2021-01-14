/*
package de.yochyo.booruapi.manager

import de.yochyo.booruapi.api.Post
import de.yochyo.eventcollection.EventCollection
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ManagerAND(private val manager: IManager, private val filterTags: Collection<String>, override val limit: Int) : IManager {
    private val mutex = Mutex()

    override val posts: EventCollection<Post> = EventCollection(ArrayList())

    /**
     * @return returns empty list on end and error or null if everything was filtered
     */
    override suspend fun downloadNextPages(amount: Int): List<Post>? {
        return mutex.withLock {
            val pages = manager.downloadNextPages(amount) ?: return@withLock null
            val filtered = pages.filter {
                for (tag in filterTags)
                    if (!it.tagString.contains(" $tag ")) return@filter false
                true
            }
            //return null (error) if everything was filtered
            if(pages.isNotEmpty() && filtered.isEmpty()) return null
            posts += filtered
            filtered
        }
    }

    override suspend fun downloadNextPage(): List<Post>? {
        return downloadNextPages(1)
    }

    override suspend fun clear() {
        mutex.withLock {
            posts.clear()
            manager.clear()
        }
    }

    override fun toString(): String {
        return "$manager AND ${filterTags.joinToString(" AND ") { it }}"
    }
}
 */