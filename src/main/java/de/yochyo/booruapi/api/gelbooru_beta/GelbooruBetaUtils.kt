package de.yochyo.booruapi.api.gelbooru_beta

import de.yochyo.booruapi.api.BooruUtils
import de.yochyo.booruapi.api.moebooru.MoebooruTag
import java.util.*

//TODO comments
object GelbooruBetaUtils {
    suspend fun parseTagsfromURL(host: String, id: Int): List<GelbooruBetaTag> {
        val lines = BooruUtils.getUrlSource("$host/index.php?page=post&s=view&id=$id")

        val tags = ArrayList<GelbooruBetaTag>()
        fun getCurrentTagType(type: String): Int {
            if (type.contains("tag-type-general")) return GelbooruBetaTag.GELBOORU_BETA_GENERAL
            if (type.contains("tag-type-artist")) return GelbooruBetaTag.GELBOORU_BETA_ARTIST
            if (type.contains("tag-type-copyright")) return GelbooruBetaTag.GELBOORU_BETA_COPYRIGHT
            if (type.contains("tag-type-character")) return GelbooruBetaTag.GELBOORU_BETA_CHARACTER
            if (type.contains("tag-type-style")) return GelbooruBetaTag.GELBOORU_BETA_META
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
            if (line.contains("<ul id=\"tag-sidebar")) {
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
                    val nameSubstring = subStringType.substring(subStringType.indexOf("tags=") + 5)
                    val name = nameSubstring.substring(nameSubstring.indexOf(">") + 1, nameSubstring.indexOf("<")).replace(" ", "_")
                    tags += GelbooruBetaTag(0, type, 0, name, false)
                }
            } catch (e: Exception) {
            }
        }
        return tags
    }
}