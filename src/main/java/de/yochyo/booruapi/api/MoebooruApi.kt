package de.yochyo.booruapi.api

import de.yochyo.booruapi.objects.Post
import de.yochyo.booruapi.objects.Tag
import de.yochyo.booruapi.utils.parseUFT8
import de.yochyo.json.JSONArray
import de.yochyo.json.JSONObject
import de.yochyo.utils.DownloadUtils
import kotlinx.coroutines.runBlocking
import java.security.MessageDigest

class MoebooruApi(url: String) : DanbooruApi(url) {
    private val utils = MoebooruUtils()


    override suspend fun getMatchingTags(beginSequence: String, limit: Int): List<Tag>? {
        val json = DownloadUtils.getJson("${url}tag.json?name=$beginSequence*&limit=$limit&search[order]=count")
        return json?.mapNotNull { if (it is JSONObject) getTagFromJson(it) else null }
    }

    override suspend fun getPosts(page: Int, tags: Array<String>, limit: Int): List<Post>? {
        val urlBuilder = StringBuilder().append("${url}post.json?limit=$limit&page=$page&login=$username&password_hash=$password")
        if (tags.isNotEmpty()) urlBuilder.append("&tags=${parseUFT8(tags.joinToString(" ") { it })}")

        val json = DownloadUtils.getJson(urlBuilder.toString())
        return json?.mapNotNull { if (it is JSONObject) getPostFromJson(it) else null }
    }

    override suspend fun getTag(name: String): Tag? {
        val json = if(name == "*") JSONArray() else DownloadUtils.getJson("${url}tag.json?name=${parseUFT8(name)}*")
        return when {
            json == null -> null
            json.isEmpty -> {
                val newestID = newestID()
                return if (newestID != null) Tag(this, name, Tag.UNKNOWN, newestID)
                else null
            }
            else -> getTagFromJson(json.getJSONObject(0))
        }
    }

    override fun getPostFromJson(json: JSONObject): Post? {
        return try {
            with(json) {
                return object : Post(getInt("id"), getString("file_url").substringAfterLast("."), getInt("width"), getInt("height"),
                        getString("rating"), getInt("file_size"), getString("file_url"),
                        getString("sample_url"), getString("preview_url"), emptyList(), getString("tags"), this@MoebooruApi) {
                    private var tagsWithType: List<Tag>? = null
                    override val tags: List<Tag>
                        get() {
                            if (tagsWithType == null)
                                tagsWithType = runBlocking { utils.parseTagsfromURL(url, getInt("id")) }
                            return tagsWithType!!
                        }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun login(username: String, password: String): Boolean {
        this.username = username
        this.password = passwordToHash(password)
        return true
    }

    override fun getTagFromJson(json: JSONObject): Tag? {
        return try {
            Tag(this, json.getString("name"), json.getInt("type"), json.getInt("count"))
        } catch (e: Exception) {
            null
        }
    }

    protected open fun passwordToHash(password: String): String {
        val byteArray = "choujin-steiner--$password--".toByteArray(charset = Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-1")
        digest.update(byteArray)
        val digestBytes = digest.digest()
        val digestStr = StringBuilder()
        for (b in digestBytes)
            digestStr.append(String.format("%02x", b))
        return digestStr.toString()
    }

    private inner class MoebooruUtils {
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
                        tags += Tag(this@MoebooruApi, name, type, 0)
                    }
                } catch (e: Exception) {
                }
            }
            return tags
        }
    }
}