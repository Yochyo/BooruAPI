package de.yochyo.booruapi.api

import de.yochyo.booruapi.objects.Post
import de.yochyo.booruapi.objects.Tag
import de.yochyo.booruapi.utils.parseURL
import de.yochyo.booruapi.utils.passwordToHash
import de.yochyo.utils.DownloadUtils
import org.json.JSONObject
import java.net.URLEncoder

abstract class Api(url: String) {
    val url: String = parseURL(url)

    protected var username = ""
    protected var passwordHash = ""

    fun login(username: String, password: String) {
        this.username = username
        this.passwordHash = if (password == "") "" else passwordToHash(password)
    }

    companion object {
        const val DEFAULT_POST_LIMIT = 30
        const val DEFAULT_TAG_LIMIT = 10
    }

    abstract fun getMatchingTagsUrl(beginSequence: String, limit: Int): String
    abstract fun getTagUrl(name: String): String
    abstract fun getPostsUrl(page: Int, tags: Array<String>, limit: Int): String

    abstract suspend fun tagFromJson(json: JSONObject): Tag?
    abstract suspend fun postFromJson(json: JSONObject): Post?

    open suspend fun getMatchingTags(beginSequence: String, amount: Int = DEFAULT_TAG_LIMIT): List<Tag> {
        val array = ArrayList<Tag>(amount)
        val json = DownloadUtils.getJson(getMatchingTagsUrl(parseToUrl(beginSequence), amount))
        if (json != null) {
            for (i in 0 until json.length()) {
                val tag = tagFromJson(json.getJSONObject(i))
                if (tag != null) array.add(tag)
            }
        }
        return array
    }

    open suspend fun getTag(name: String): Tag {
        if (name == "*") return Tag(name, Tag.SPECIAL, this, count = newestID())
        val json = DownloadUtils.getJson(getTagUrl(parseToUrl(name)))
        if (json != null && json.length() > 0) {
            val tag = tagFromJson(json.getJSONObject(0))
            if (tag != null) return tag
        }
        return Tag(name, if (Tag.isSpecialTag(name)) Tag.SPECIAL else Tag.UNKNOWN, this)
    }

    open suspend fun getPosts(page: Int, tags: Array<String>, limit: Int = DEFAULT_POST_LIMIT): List<Post>? {
        val posts = ArrayList<Post>(limit)
        val urlBuilder = StringBuilder().append(getPostsUrl(page, tags, limit))
        if (tags.isNotEmpty()) {
            urlBuilder.append("&tags=")
            urlBuilder.append(parseToUrl(tags.joinToString(" ") { it }))
        }
        println("[$urlBuilder]")
        val json = DownloadUtils.getJson(urlBuilder.toString())
        return if (json != null) {
            for (i in 0 until json.length()) {
                val post = postFromJson(json.getJSONObject(i))
                if (post != null) posts += post
            }
            posts
        } else null
    }

    open suspend fun newestID(): Int {
        val json = DownloadUtils.getJson(getPostsUrl(1, arrayOf("*"), 1))
        return json?.getJSONObject(0)?.getInt("id") ?: 0
    }

    protected fun parseToUrl(urlStr: String): String {
        return URLEncoder.encode(urlStr, "UTF-8")
    }
}