package de.yochyo.booruapi.manager

import de.yochyo.booruapi.objects.Post

open class ManagerUtils{
    open fun addPageTo(to: MutableCollection<Post>, page: List<Post>): Boolean{
        val lastPostID = if (to.isNotEmpty()) to.last().id else Integer.MAX_VALUE
        to += page.takeLastWhile { lastPostID > it.id }
        return true
    }
}