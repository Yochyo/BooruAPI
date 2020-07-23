package de.yochyo.booruapi.api

import de.yochyo.booruapi.objects.Post
import de.yochyo.booruapi.objects.Tag
import de.yochyo.json.JSONObject
import de.yochyo.utils.DownloadUtils
import kotlinx.coroutines.runBlocking

open class GelbooruApi(val url: String) : IBooruApi {
    private var username = ""
    private var password = ""

    val urlHost: String = try {
        if (url == "") ""
        else java.net.URL(url).host
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }

    override val DEFAULT_POST_LIMIT: Int = 30
    override val DEFAULT_TAG_LIMIT: Int = 10

    override suspend fun login(username: String, password: String): Boolean {
        this.username = username
        this.password = password
        return true;
    }

    override suspend fun getMatchingTags(beginSequence: String, limit: Int): List<Tag>? {
        val url = "$url/index.php?page=dapi&s=tag&q=index&json=1&api_key=$password&user_id=$username&limit=$limit&name_pattern=$beginSequence%"
        val json = DownloadUtils.getJson(url)
        return json?.mapNotNull { if (it is JSONObject) getTagFromJson(it) else null }
    }

    override suspend fun getTag(name: String): Tag? {
        val url = "$url/index.php?page=dapi&s=tag&q=index&json=1&api_key=$password&user_id=$username&limit=1&name=$name"
        val json = DownloadUtils.getJson(url)
        return when {
            json == null -> null
            json.isEmpty -> {
                val newestID = newestID()
                return if (newestID != null) Tag(this, name, Tag.UNKNOWN, newestID)
                else null
            }
            else -> getTagFromJson(json[0] as JSONObject)
        }
    }

    override suspend fun getPosts(page: Int, tags: Array<String>, limit: Int): List<Post>? {
        val pid = (page - 1)
        val url = "$url/index.php?page=dapi&s=post&q=index&json=1&api_key=$password&user_id=$username&limit=$limit&pid=$pid&tags=${tags.joinToString(" ")}"
        val json = DownloadUtils.getJson(url)
        return json?.mapNotNull { if (it is JSONObject) getPostFromJson(it) else null }
    }

    override suspend fun newestID() = getPosts(1, arrayOf("*"), 1)?.firstOrNull()?.id

    override fun createPost(id: Int, extention: String, width: Int, height: Int, rating: String,
                            fileSize: Int, fileURL: String, fileSampleURL: String, filePreviewURL:
                            String, tags: List<Tag>, tagString: String): Post? {
        return object : Post(id, extention, width, height,
                rating, fileSize, fileURL,
                fileSampleURL, filePreviewURL, tags, tagString, this) {
            private var tagsWithType: List<Tag>? = if (tags.isEmpty()) null else tags
            override val tags: List<Tag>
                get() {
                    if (tagsWithType == null)
                        tagsWithType = runBlocking { getTags(tagString.split(" ")) }
                    return tagsWithType!!
                }
        }
    }

    public suspend fun getTags(names: List<String>): List<Tag>? {
        val url = "$url/index.php?page=dapi&s=tag&q=index&json=1&api_key=$password&user_id=$username&limit=${names.size}&names=${names.joinToString(" ")}"
        val json = DownloadUtils.getJson(url)
        return when {
            json == null -> null
            json.isEmpty -> emptyList()
            else -> json.mapNotNull { if (it is JSONObject) getTagFromJson(it) else null }
        }
    }

    protected open fun getPostFromJson(json: JSONObject): Post? {
        return with(json) {
            val sampleUrl =

                    if (getInt("sample") == 1) "https://img1.$urlHost/samples/${getString("directory")}/sample_${getString("hash")}.jpg"
                    else getString("file_url")
            createPost(getInt("id"), getString("file_url").extention() ?: "", getInt("width"),
                    getInt("height"), getString("rating"), -1, getString("file_url"),
                    sampleUrl,
                    "https://img1.$urlHost/thumbnails/${getString("directory")}/thumbnail_${getString("hash")}.jpg",
                    emptyList(), getString("tags")
            )
        }

    }

    protected open fun getTagFromJson(json: JSONObject): Tag? {
        val type = when (json.getString("type")) {
            "copyright" -> Tag.COPYPRIGHT
            "metadata" -> Tag.META
            "character" -> Tag.CHARACTER
            "tag" -> Tag.GENERAL
            "artist" -> Tag.ARTIST
            else -> Tag.UNKNOWN
        }
        return Tag(this, json.getString("tag"), type, json.getInt("count"))
    }


    private fun String.extention(): String? {
        val lastindex = this.lastIndexOf(".")
        return if (lastindex > 0) substring(lastIndex + 1) else null
    }
}