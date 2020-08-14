package de.yochyo.booruapi.api

import de.yochyo.booruapi.objects.Post
import de.yochyo.booruapi.objects.Tag

interface IBooruApi {
    val DEFAULT_POST_LIMIT: Int
    val DEFAULT_TAG_LIMIT: Int

    suspend fun login(username: String, password: String): Boolean
    suspend fun getMatchingTags(beginSequence: String, limit: Int = DEFAULT_TAG_LIMIT): List<Tag>?
    suspend fun getTag(name: String): Tag?
    suspend fun getPosts(page: Int, tags: Array<String>, limit: Int = DEFAULT_POST_LIMIT): List<Post>?
    suspend fun newestID(): Int?

    fun createPost(id: Int, extention: String, width: Int, height: Int, rating: String, fileSize: Int,
                           fileURL: String, fileSampleURL: String, filePreviewURL: String,
                           tags: List<Tag>, tagString: String): Post?
}