package de.yochyo.booruapi.manager

import de.yochyo.booruapi.api.Post

object ManagerUtils {
    open fun addAndRemoveDuplicates(to: MutableCollection<Post>, page: List<Post>): List<Post> {
        val lastPostID = if (to.isNotEmpty()) to.last().id else Integer.MAX_VALUE
        val res = page.takeLastWhile { lastPostID > it.id }
        to += res
        return res
    }
}