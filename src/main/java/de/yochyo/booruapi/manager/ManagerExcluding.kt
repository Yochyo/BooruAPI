package de.yochyo.booruapi.manager

import de.yochyo.booruapi.objects.Post
import de.yochyo.eventcollection.EventCollection
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ManagerExcluding(private val manager: IManager, private val excluding: Collection<String>) : IManager by manager {
    override val posts: EventCollection<Post> = EventCollection(ArrayList())
    private val mutex = Mutex()

    override suspend fun downloadNextPages(amount: Int): List<Post>? {
        return mutex.withLock {
            var pages = manager.downloadNextPages(amount)
            if (pages != null) {
                pages = pages.filter {
                    for (ex in excluding) if (it.tagString.contains(" $ex ")) return@filter false
                    true
                }
                posts += pages
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
        return "NOT( ${excluding.joinToString(" ") { it }} ) $manager"
    }
}