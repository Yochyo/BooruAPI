package de.yochyo.booruapi.manager

import de.yochyo.booruapi.api.Post
import de.yochyo.eventcollection.EventCollection

interface IManager {
    val limit: Int
    val posts: EventCollection<Post>

    /**
     * @return returns null on error or end, returns null if the download succeeded but the result list would be empty
     */
    suspend fun downloadNextPage(): List<Post>?
    suspend fun downloadNextPages(amount: Int): List<Post>?
    suspend fun clear()
}