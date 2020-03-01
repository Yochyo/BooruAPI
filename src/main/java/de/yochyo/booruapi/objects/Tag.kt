package de.yochyo.booruapi.objects

import de.yochyo.booruapi.api.Api

data class Tag(val name: String, val type: Int, val api: Api, val count: Int = 0): Comparable<Tag> {

    companion object {
        const val GENERAL = 0
        const val CHARACTER = 4
        const val COPYPRIGHT = 3
        const val ARTIST = 1
        const val META = 5
        const val UNKNOWN = 99
        const val SPECIAL = 100

        fun isSpecialTag(name: String): Boolean {
            return name == "*" || name.startsWith("height") || name.startsWith("width") || name.startsWith("order") || name.startsWith("rating") || name.contains(" ")
        }

        fun getCorrectTagType(tagName: String, type: Int): Int {
            return if (type in 0..1 || type in 3..5) type
            else if (isSpecialTag(tagName)) SPECIAL
            else UNKNOWN
        }
    }

    override fun toString(): String = name
    override fun compareTo(other: Tag): Int {
        return name.compareTo(other.name)
    }

}