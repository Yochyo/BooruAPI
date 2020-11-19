package de.yochyo.booruapi.api.gelbooru

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.yochyo.booruapi.api.BooruUtils
import de.yochyo.booruapi.api.IBooruApi
import de.yochyo.booruapi.api.Tag
import de.yochyo.booruapi.utils.encodeUTF8
import de.yochyo.json.JSONObject
import java.text.SimpleDateFormat

open class GelbooruApi(val url: String) : IBooruApi {
    private val mapper = JsonMapper.builder().apply {
        addModule(KotlinModule())
        defaultDateFormat(SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy"))
        propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }.build()

    private var username = ""
    private var password = ""

    override suspend fun login(username: String, password: String): Boolean {
        this.username = username
        this.password = password
        return true;
    }

    override suspend fun getTagAutoCompletion(begin: String, limit: Int): List<GelbooruTag>? {
        val url = "$url/index.php?page=dapi&s=tag&q=index&json=1&api_key=$password&user_id=$username&limit=$limit&name_pattern=${encodeUTF8(begin)}%"
        val json = BooruUtils.getJsonArrayFromUrl(url)
        return json?.mapNotNull { if (it is JSONObject) parseTagFromJson(it) else null }
    }

    override suspend fun getTag(name: String): GelbooruTag? {
        val url = "$url/index.php?page=dapi&s=tag&q=index&json=1&api_key=$password&user_id=$username&limit=1&name=${encodeUTF8(name)}"
        val json = BooruUtils.getJsonArrayFromUrl(url)
        return when {
            json == null -> null
            json.isEmpty -> {
                val newestID = getNewestPost()?.id
                return if (newestID != null) GelbooruTag(-1, name, 0, GelbooruTag.GELBOORU_UNKNOWN, false)
                else null
            }
            else -> parseTagFromJson(json.getJSONObject(0))
        }
    }

    override suspend fun getPosts(page: Int, tags: String, limit: Int): List<GelbooruPost>? {
        val pid = (page - 1)
        val url = "$url/index.php?page=dapi&s=post&q=index&json=1&api_key=$password&user_id=$username&limit=$limit&pid=$pid&tags=${encodeUTF8(tags)}"
        val json = BooruUtils.getJsonArrayFromUrl(url)
        return json?.mapNotNull { if (it is JSONObject) parsePostFromJson(it) else null }
    }

    suspend fun getTags(names: String): List<Tag>? {
        val url = "$url/index.php?page=dapi&s=tag&q=index&json=1&api_key=$password&user_id=$username&limit=${names.split(" ").size}&names=${encodeUTF8(names)}"
        val json = BooruUtils.getJsonArrayFromUrl(url)
        return when {
            json == null -> null
            json.isEmpty -> emptyList()
            else -> json.mapNotNull { if (it is JSONObject) parseTagFromJson(it) else null }
        }
    }


    fun parsePostFromJson(json: JSONObject): GelbooruPost? = try {
        mapper.readValue(json.toString(), GelbooruPost::class.java).apply {
            gelbooruApi = this@GelbooruApi
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    fun parseTagFromJson(json: JSONObject): GelbooruTag? = try {
        mapper.readValue(json.toString(), GelbooruTag::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}