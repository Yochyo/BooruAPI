package de.yochyo.booruapi.api.moebooru

import de.yochyo.booruapi.api.BooruUtils
import de.yochyo.booruapi.api.Tag
import de.yochyo.booruapi.api.TagType
import java.util.*

//TODO comments
object MoebooruUtils {
    suspend fun parseTagsfromURL(host: String, id: Int): List<MoebooruTag> {
        val lines = BooruUtils.getUrlSource("$host/post/show/$id")

        val tags = ArrayList<MoebooruTag>()
        fun getCurrentTagType(type: String): Int {
            if (type.contains("tag-type-general")) return MoebooruTag.MOEBOORU_GENERAL
            if (type.contains("tag-type-artist")) return MoebooruTag.MOEBOORU_ARTIST
            if (type.contains("tag-type-copyright")) return MoebooruTag.MOEBOORU_COPYRIGHT
            if (type.contains("tag-type-character")) return MoebooruTag.MOEBOORU_CHARACTER
            if(type.contains("tag-type-circle"))return MoebooruTag.MOEBOORU_CIRCLE
            if (type.contains("tag-type-style")) return MoebooruTag.MOEBOORU_META
            return MoebooruTag.MOEBOORU_UNKNOWN
        }

        fun String.startWithIgnoreSpace(start: String): Boolean {
            val a = toCharArray()
            for (i in a.indices) {
                if (a[i] != ' ')
                    return startsWith(start, startIndex = i)
            }
            return false
        }

        var nextLine = false
        val builder = StringBuilder()

        for (line in lines) {
            if (line.startWithIgnoreSpace("<ul id=\"tag-sideb")) {
                nextLine = true
                continue
            }
            if (nextLine)  //table with tag is appended to String
                if (line.startWithIgnoreSpace("</ul>"))
                    break
                else builder.append(line)
        }


        //tags are extracted from table
        val splitLines = builder.toString().split("</li>")
        for (l in splitLines) {
            try {
                val subStringType = l.substring(l.indexOf("<li class=\"") + 11)
                val type = getCurrentTagType(subStringType.substring(0, subStringType.indexOf("\"")))

                if (type != MoebooruTag.MOEBOORU_UNKNOWN) {
                    val nameSubstring = subStringType.substring(subStringType.indexOf("href=\"/post?") + 12)
                    val name = nameSubstring.substring(nameSubstring.indexOf(">") + 1, nameSubstring.indexOf("<")).replace(" ", "_")
                    tags += MoebooruTag(0, name, 0, type, false)
                }
            } catch (e: Exception) {
            }
        }
        return tags
    }
}