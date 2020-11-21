package de.yochyo.booruapi.api.gelbooru

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.yochyo.booruapi.api.BooruUtils
import de.yochyo.booruapi.api.IBooruApi
import de.yochyo.booruapi.utils.encodeUTF8
import de.yochyo.json.JSONArray
import de.yochyo.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

open class GelbooruApi(val host: String) : IBooruApi {
    private val mapper = JsonMapper.builder().apply {
        addModule(KotlinModule())
        defaultDateFormat(SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.UK))
        propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
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
        val url = "$host/index.php?page=dapi&s=tag&q=index&json=1&api_key=$password&user_id=$username&limit=$limit&name_pattern=${encodeUTF8(begin)}%"
        val json = BooruUtils.getJsonArrayFromUrl(url)
        return json?.mapNotNull { if (it is JSONObject) parseTagFromJson(it) else null }
    }

    override suspend fun getTag(name: String): GelbooruTag? {
        val url = "$host/index.php?page=dapi&s=tag&q=index&json=1&api_key=$password&user_id=$username&limit=1&name=${encodeUTF8(name)}"
        val json =
                if (name == "*") JSONArray()
                else BooruUtils.getJsonArrayFromUrl(url)
        return when {
            json == null -> null
            json.isEmpty -> getDefaultTag(name)
            else -> {
                val tag = parseTagFromJson(json.getJSONObject(0))
                if (tag?.name == name) tag
                else getDefaultTag(name)
            }
        }
    }

    private suspend fun getDefaultTag(name: String): GelbooruTag? {
        val newestID = getNewestPost()?.id
        return if (newestID != null) GelbooruTag(-1, name, newestID, GelbooruTag.GELBOORU_UNKNOWN, false)
        else null
    }

    override suspend fun getPosts(page: Int, tags: String, limit: Int): List<GelbooruPost>? {
        val pid = (page - 1)
        val url = "$host/index.php?page=dapi&s=post&q=index&json=1&api_key=$password&user_id=$username&limit=$limit&pid=$pid&tags=${encodeUTF8(tags)}"
        val json = BooruUtils.getJsonArrayFromUrl(url)
        return json?.mapNotNull { if (it is JSONObject) parsePostFromJson(it) else null }
    }

    suspend fun getTags(names: String): List<GelbooruTag>? {
        val url = "$host/index.php?page=dapi&s=tag&q=index&json=1&api_key=$password&user_id=$username&limit=${names.split(" ").size}&names=${encodeUTF8(names)}"
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