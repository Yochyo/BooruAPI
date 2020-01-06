package de.yochyo.booruapi.api

import de.yochyo.booruapi.objects.Post
import de.yochyo.booruapi.objects.Tag
import de.yochyo.utils.DownloadUtils
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class MoebooruApi(url: String) : Api(url) {
    override fun urlGetTag(name: String): String = "${url}tag.json?name=$name*"
    override fun urlGetTags(beginSequence: String): String {
        return "${url}tag.json?name=$beginSequence*&limit=$DEFAULT_TAG_LIMIT&search[order]=count"
    }

    override fun urlGetPosts(page: Int, tags: Array<String>, limit: Int): String {
        return "${url}post.json?limit=$limit&page=$page&login=$username&password_hash=$passwordHash"
    }

    override suspend fun getPostFromJson(json: JSONObject): Post? {
        return try {
            with(json) {
                return object : Post(getInt("id"), getString("file_url").substringAfterLast("."), getInt("width"), getInt("height"),
                        getString("rating"), getInt("file_size"), getString("file_url"),
                        getString("sample_url"), getString("preview_url"), emptyList()) {
                    private var tagsWithType: List<Tag>? = null
                    override val tags: List<Tag>
                        get() {
                            if (tagsWithType == null)
                                tagsWithType = runBlocking { MoebooruUtils.parseTagsfromURL(url, getInt("id")) }
                            return tagsWithType!!
                        }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getTagFromJson(json: JSONObject): Tag? {
        return try {
            val name = json.getString("name")
            Tag(name, json.getInt("type"), json.getInt("count"))
        } catch (e: Exception) {
            null
        }
    }
}

private object MoebooruUtils {
    suspend fun parseTagsfromURL(apiUrl: String, id: Int): List<Tag> {
        val lines = DownloadUtils.getUrlSource("$apiUrl/post/show/$id")

        val tags = ArrayList<Tag>()
        fun getCurrentTagType(type: String): Int {
            if (type.contains("tag-type-general")) return Tag.GENERAL
            if (type.contains("tag-type-artist")) return Tag.ARTIST
            if (type.contains("tag-type-copyright")) return Tag.COPYPRIGHT
            if (type.contains("tag-type-character")) return Tag.CHARACTER
            return Tag.UNKNOWN
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
            if (nextLine)  //hier wird die Tag-Tabelle in einen String gesammelt
                if (line.startWithIgnoreSpace("</ul>"))
                    break
                else builder.append(line)
        }


        //Die Tags werden aus der Tabelle extrahiert
        val splitLines = builder.toString().split("</li>")
        for (l in splitLines) {
            try {
                val subStringType = l.substring(l.indexOf("<li class=\"") + 11)
                val type = getCurrentTagType(subStringType.substring(0, subStringType.indexOf("\"")))

                if (type != Tag.UNKNOWN) {
                    val nameSubstring = subStringType.substring(subStringType.indexOf("href=\"/post?") + 12)
                    val name = nameSubstring.substring(nameSubstring.indexOf(">") + 1, nameSubstring.indexOf("<")).replace(" ", "_")
                    tags += Tag(name, type)
                }
            } catch (e: Exception) {
            }
        }
        return tags
    }
}