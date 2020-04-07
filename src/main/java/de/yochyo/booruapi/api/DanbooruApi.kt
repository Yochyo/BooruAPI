package de.yochyo.booruapi.api

import de.yochyo.booruapi.objects.Post
import de.yochyo.booruapi.objects.Tag
import de.yochyo.booruapi.utils.parseUFT8
import de.yochyo.booruapi.utils.parseURL
import de.yochyo.json.JSONObject
import de.yochyo.utils.DownloadUtils
import java.security.MessageDigest

open class DanbooruApi(url: String) : IApi {
    open val url = parseURL(url)

    protected var username = ""
    protected var password = ""
    override val DEFAULT_POST_LIMIT = 30
    override val DEFAULT_TAG_LIMIT = 10

    override suspend fun getMatchingTags(beginSequence: String, limit: Int): List<Tag>? {
        val json = DownloadUtils.getJson("${url}tags.json?search[name_matches]=$beginSequence*&limit=$limit&search[order]=count")
        return json?.mapNotNull { if (it is JSONObject) getTagFromJson(it) else null }
    }

    override suspend fun getTag(name: String): Tag? {
        val json = DownloadUtils.getJson("${url}tags.json?search[name_matches]=${parseUFT8(name)}")
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

    override suspend fun getPosts(page: Int, tags: Array<String>, limit: Int): List<Post>? {
        val urlBuilder = StringBuilder().append("${url}posts.json?limit=$limit&page=$page&login=$username&password_hash=$password")
        if (tags.isNotEmpty()) urlBuilder.append("&tags=${parseUFT8(tags.joinToString(" ") { it })}")

        val json = DownloadUtils.getJson(urlBuilder.toString())
        return json?.mapNotNull { if (it is JSONObject) getPostFromJson(it) else null }
    }

    override suspend fun newestID() = getPosts(1, arrayOf("*"), 1)?.firstOrNull()?.id

    override suspend fun login(username: String, password: String): Boolean {
        this.username = username
        this.password = passwordToHash(password)
        return true
    }


    protected open fun getTagFromJson(json: JSONObject): Tag? {
        return try {
            Tag(this, json.getString("name"), json.getInt("category"), json.getInt("post_count"))
        } catch (e: Exception) {
            null
        }
    }

    protected open fun getPostFromJson(json: JSONObject): Post? {
        return try {
            with(json) {
                val tagsGeneral = getString("tag_string_general").split(" ").filter { it != "" }.map { Tag(this@DanbooruApi, it, Tag.GENERAL, 0) }
                val tagsCharacter = getString("tag_string_character").split(" ").filter { it != "" }.map { Tag(this@DanbooruApi, it, Tag.CHARACTER, 0) }
                val tagsCopyright = getString("tag_string_copyright").split(" ").filter { it != "" }.map { Tag(this@DanbooruApi, it, Tag.COPYPRIGHT, 0) }
                val tagsArtist = getString("tag_string_artist").split(" ").filter { it != "" }.map { Tag(this@DanbooruApi, it, Tag.ARTIST, 0) }
                val tagsMeta = getString("tag_string_meta").split(" ").filter { it != "" }.map { Tag(this@DanbooruApi, it, Tag.META, 0) }
                val tags = ArrayList<Tag>(tagsGeneral.size + tagsCharacter.size + tagsCopyright.size + tagsArtist.size + tagsMeta.size)
                tags += tagsArtist
                tags += tagsCopyright
                tags += tagsCharacter
                tags += tagsGeneral
                tags += tagsMeta
                Post(getInt("id"), getString("file_ext"), getInt("image_width"), getInt("image_height"),
                        getString("rating"), getInt("file_size"), getString("file_url"),
                        getString("large_file_url"), getString("preview_file_url"), tags, getString("tag_string"), this@DanbooruApi)
            }
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
}