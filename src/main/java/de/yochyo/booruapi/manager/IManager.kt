package de.yochyo.booruapi.manager

import de.yochyo.booruapi.objects.Post
import de.yochyo.eventcollection.EventCollection

interface IManager {
    val limit: Int
    val posts: EventCollection<Post>

    suspend fun downloadNextPage(): List<Post>?
    suspend fun downloadNextPages(amount: Int): List<Post>?
    suspend fun clear()
}