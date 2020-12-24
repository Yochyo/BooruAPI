package de.yochyo.booruapi.api.pixiv

import de.yochyo.pixiv_api.PixivApi
import de.yochyo.pixiv_api.response_types.PixivIllustSearch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*


class PixivIllustSearchList(val api: PixivApi, search: PixivIllustSearch) {
    private val searches = LinkedList<PixivIllustSearch>()
    private val mutex = Mutex()

    init {
        searches.add(search)
    }

    suspend fun get(page: Int): PixivIllustSearch? {
        return mutex.withLock {
            val index = page - 1
            if (index < 0) throw IndexOutOfBoundsException("index $index lower than 0")
            if (index in searches.indices) return searches[index]

            val iter = searches.last.iterator(api)
            while (iter.hasNext() && searches.size < page) {
                searches.add(iter.next())
            }
            return if (searches.last.nextUrl == null && searches.size <= page) searches.last.copy(illusts = emptyList())
            else if (searches.size == page) searches.last else null
        }
    }
}