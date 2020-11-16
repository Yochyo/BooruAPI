package de.yochyo.booruapi.api.danbooru

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import danbooru.DanbooruPost
import de.yochyo.booruapi.api.BooruUtils
import de.yochyo.booruapi.api.IBooruApi
import de.yochyo.booruapi.api.TagType
import de.yochyo.booruapi.utils.encodeUTF8
import de.yochyo.json.JSONArray
import de.yochyo.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class DanbooruApi(val host: String) : IBooruApi {
    private val mapper = JsonMapper.builder().apply {
        addModule(KotlinModule())
        defaultDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
        propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }.build()

    protected var username = ""
    protected var apiKey = ""

    override suspend fun getTag(name: String): DanbooruTag? {
        val json =
                if (name == "*") JSONArray()
                else BooruUtils.getJsonArrayFromUrl("${host}tags.json?search[name_matches]=${encodeUTF8(name)}")

        return when {
            json == null || json.isEmpty -> null
            json.isEmpty -> {
                val newestCount = getNewestPost()?.id
                return if (newestCount == null) null
                else DanbooruTag(0, "*", newestCount, DanbooruTag.DANBOORU_UNKNOWN, Date(Long.MIN_VALUE), Date(Long.MIN_VALUE), false)
            }
            else -> parseTagFromJson(json.getJSONObject(0))
        }
    }

    override suspend fun getTagAutoCompletion(begin: String, limit: Int): List<DanbooruTag>? {
        val url = "${host}tags.json?search[name_matches]=${encodeUTF8(begin)}*&limit=$limit&search[order]=count"
        val json = BooruUtils.getJsonArrayFromUrl(url)
        return json?.mapNotNull { if (it is JSONObject) parseTagFromJson(it) else null }
    }

    override suspend fun getPosts(page: Int, tags: String, limit: Int): List<DanbooruPost>? {
        val url = "${host}posts.json?limit=$limit&page=$page&login=$username&api_key=$apiKey&tags=${encodeUTF8(tags)}"
        val json = BooruUtils.getJsonArrayFromUrl(url)
        return json?.mapNotNull { if (it is JSONObject) parsePostFromJson(it) else null }
    }

    fun parsePostFromJson(json: JSONObject): DanbooruPost = mapper.readValue(json.toString(), DanbooruPost::class.java)
    fun parseTagFromJson(json: JSONObject): DanbooruTag = mapper.readValue(json.toString(), DanbooruTag::class.java)

    override suspend fun login(username: String, password: String): Boolean {
        this.username = username
        this.apiKey = password
        return true
    }


}