package de.yochyo.booruapi.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


interface IBooruApi {
    val host: String

    /**
     * Will either login the user or allow him/her to access data with his/her account.
     * @param username Username. Depending on the Api, it may be an id or email
     * @param password Password. Depending on the Api, it may be an api-key or token.
     * @return true if success
     */
    suspend fun login(username: String, password: String): Boolean

    /**
     * Returns a Tag with a specific name
     * @param name The full name of the tag
     * @return The tag with the name "name".
     * If the tag does not exist, All fields will have default values (type = TagType.UNKNOWN, count = ID of newest Post)
     */
    suspend fun getTag(name: String): Tag

    /**
     * Returns a List of size "limit" or less with Tags starting with "begin"
     * @param begin Begin of Tags you want the AutoCompletion of.
     * @param limit of autocompleted tags that should be returned.
     * @return List of tags that begin with "begin"
     */
    suspend fun getTagAutoCompletion(begin: String, limit: Int): List<Tag>?

    /**
     * Returns a List of posts
     * @param page page of posts, the first page should always have the index 1
     * @param limit Limit of posts a page can contain. Depending on the api, the limit may be limited
     * @return Returns a List of posts or null if error. Returns an empty list if reached end
     */
    suspend fun getPosts(page: Int, tags: String, limit: Int): List<Post>?
    suspend fun getNewestPost(): Post? = getPosts(1, "*", 1)?.firstOrNull()

    suspend fun getUrlInputStream(url: String): InputStream? {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val conn = URL(url).openConnection() as HttpURLConnection
                for (header in getHeaders()) conn.addRequestProperty(header.key, header.value)
                conn.requestMethod = "GET"
                val input = conn.inputStream
                input
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun getHeaders(): Map<String, String> {
        return mapOf(Pair("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:84.0) Gecko/20100101 Firefox/84.0"))
    }
}
