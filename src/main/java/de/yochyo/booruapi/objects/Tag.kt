package de.yochyo.booruapi.objects

import de.yochyo.booruapi.api.IApi

open class Tag(open val api: IApi, open val name: String, open val type: Int, open val count: Int) : Comparable<Tag> {

    companion object {
        const val GENERAL = 0
        const val CHARACTER = 4
        const val COPYPRIGHT = 3
        const val ARTIST = 1
        const val META = 5
        const val UNKNOWN = 99
    }

    override fun toString(): String = name
    override fun compareTo(other: Tag) = name.compareTo(other.name)

}