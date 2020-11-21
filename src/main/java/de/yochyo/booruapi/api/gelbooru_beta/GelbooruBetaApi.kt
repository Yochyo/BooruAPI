package de.yochyo.booruapi.api.gelbooru_beta

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.yochyo.booruapi.api.BooruUtils
import de.yochyo.booruapi.api.IBooruApi
import de.yochyo.booruapi.utils.encodeUTF8
import de.yochyo.json.JSONArray
import de.yochyo.json.JSONObject
import de.yochyo.json.XML
import java.text.SimpleDateFormat
import java.util.*

open class GelbooruBetaApi(val host: String) : IBooruApi {
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

    override suspend fun getTagAutoCompletion(begin: String, limit: Int): List<GelbooruBetaTag>? {
        val url = "$host/index.php?page=dapi&s=tag&q=index&json=1&api_key=$password&user_id=$username&limit=$limit&name_pattern=${encodeUTF8(begin)}%"
        val xml = BooruUtils.getStringFromUrl(url) ?: return null
        val jsonParent = XML.toJSONObject(xml)
        val json = jsonParent.getJSONObject("tags").let { if (it.has("tag")) it.get("tag") else JSONArray() }
        return when {
            json is JSONObject -> listOf(parseTagFromJson(json)).mapNotNull { it }
            json is JSONArray -> json.mapNotNull { if (it is JSONObject) parseTagFromJson(it) else null }
            else -> null
        }
    }

    override suspend fun getTag(name: String): GelbooruBetaTag? {
        val url = "$host/index.php?page=dapi&s=tag&q=index&json=1&api_key=$password&user_id=$username&limit=1&name=${encodeUTF8(name)}"

        val json =
                if (name == "*") JSONArray()
                else {
                    val xml = BooruUtils.getStringFromUrl(url) ?: return null
                    xml.let { XML.toJSONObject(it) }?.getJSONObject("tags")?.let { if (it.has("tag")) it.get("tag") else JSONArray() }.let {
                        if (it is JSONObject) JSONArray().apply { put(it) }
                        else if (it is JSONArray) it
                        else null
                    }
                }

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

    private suspend fun getDefaultTag(name: String): GelbooruBetaTag? {
        val newestID = getNewestPost()?.id
        return if (newestID != null) GelbooruBetaTag(-1, name, GelbooruBetaTag.GELBOORU_BETA_UNKNOWN, newestID, false)
        else null
    }

    override suspend fun getPosts(page: Int, tags: String, limit: Int): List<GelbooruBetaPost>? {
        val pid = (page - 1)
        val url = "$host/index.php?page=dapi&s=post&q=index&api_key=$password&user_id=$username&limit=$limit&pid=$pid&tags=${encodeUTF8(tags)}"
        val xml = BooruUtils.getStringFromUrl(url) ?: return null
        val jsonParent = XML.toJSONObject(xml)
        val json = jsonParent.getJSONObject("posts").let { if (it.has("post")) it.get("post") else JSONArray() }
        return when {
            json is JSONObject -> listOf(parsePostFromJson(json)).mapNotNull { it }
            json is JSONArray -> json.mapNotNull { if (it is JSONObject) parsePostFromJson(it) else null }
            else -> null
        }
    }

    fun parsePostFromJson(json: JSONObject): GelbooruBetaPost? = try {
        mapper.readValue(json.toString(), GelbooruBetaPost::class.java).apply {
            gelbooruBetaApi = this@GelbooruBetaApi
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    fun parseTagFromJson(json: JSONObject): GelbooruBetaTag? = try {
        mapper.readValue(json.toString(), GelbooruBetaTag::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}