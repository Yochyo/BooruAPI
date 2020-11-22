package de.yochyo.booruapi.api.moebooru

import de.yochyo.booruapi.BooruTagSelector
import org.jsoup.nodes.Element

//TODO comments
object MoebooruUtils {
    private val selector = object : BooruTagSelector<MoebooruTag, Int>() {

        override fun getType(element: Element): Int {
            val type = element.attr("data-type")
            return when (type) {
                "general" -> MoebooruTag.MOEBOORU_GENERAL
                "copyright" -> MoebooruTag.MOEBOORU_COPYRIGHT
                "artist" -> MoebooruTag.MOEBOORU_ARTIST
                "circle" -> MoebooruTag.MOEBOORU_CIRCLE
                "style" -> MoebooruTag.MOEBOORU_META
                "character" -> MoebooruTag.MOEBOORU_CHARACTER
                else -> MoebooruTag.MOEBOORU_UNKNOWN
            }
        }

        override fun getName(element: Element): String {
            return element.attr("data-name")
        }

        override fun toTag(name: String, type: Int): MoebooruTag {
            return MoebooruTag(0, name, type, 0, false)
        }

    }

    suspend fun parseTagsFromUrl(host: String, id: Int): List<MoebooruTag>? {
        return selector.parse("$host/post/show/$id")?.map { it.copy(name = it.name.replace(" ", "_")) }
    }
}