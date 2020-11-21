package de.yochyo.booruapi.api.my_imouto

import de.yochyo.booruapi.api.BooruUtils
import java.util.*

//TODO comments
object MyImoutoUtils {
    suspend fun parseTagsfromURL(host: String, id: Int): List<MyImoutoTag> {
        val lines = BooruUtils.getUrlSource("$host/post/show/$id")

        val tags = ArrayList<MyImoutoTag>()
        fun getCurrentTagType(type: String): Int {
            if (type.contains("tag-type-general")) return MyImoutoTag.MY_IMOUTO_GENERAL
            if (type.contains("tag-type-artist")) return MyImoutoTag.MY_IMOUTO_ARTIST
            if (type.contains("tag-type-copyright")) return MyImoutoTag.MY_IMOUTO_COPYRIGHT
            if (type.contains("tag-type-character")) return MyImoutoTag.MY_IMOUTO_CHARACTER
            if (type.contains("tag-type-circle")) return MyImoutoTag.MY_IMOUTO_CIRCLE
            if (type.contains("tag-type-faults")) return MyImoutoTag.MY_IMOUTO_META
            return MyImoutoTag.MY_IMOUTO_UNKNOWN
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

                if (type != MyImoutoTag.MY_IMOUTO_UNKNOWN) {
                    val nameSubstring = subStringType.substring(subStringType.indexOf("href=\"/post?") + 12)
                    val name = nameSubstring.substring(nameSubstring.indexOf(">") + 1, nameSubstring.indexOf("<")).replace(" ", "_")
                    tags += MyImoutoTag(0, name, type, 0, "", Date(), false)
                }
            } catch (e: Exception) {
            }
        }
        return tags
    }
}