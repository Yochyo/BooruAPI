package de.yochyo.booruapi.api.gelbooru

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.yochyo.booruapi.api.Post
import de.yochyo.booruapi.api.Tag
import de.yochyo.booruapi.utils.extension
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.util.*

//TODO comments
data class GelbooruPost(
        val source: String,
        val directory: String,
        val hash: String,
        override val height: Int,
        override val id: Int,
        @JsonProperty("image") val imageName: String,
        val change: Long,
        val owner: String,
        val parentId: Int,
        override val rating: String,
        val sample: Boolean,
        val sampleHeight: Int,
        val sampleWidth: Int,
        val score: Int,
        @JsonProperty("tags") override val tagString: String,
        override val width: Int,
        val fileUrl: String,
        val createdAt: Date,
        var gelbooruApi: GelbooruApi? = null
) : Post(
        id, imageName.extension(), width, height, rating, -1, fileUrl,
        "${URL(fileUrl).host}/samples/$directory/sample_$imageName",
        "${URL(fileUrl).host}/thumbnails/$directory/thumbnail_$imageName", tagString
) {
    private val _tags by lazy {
        if (gelbooruApi == null) super.getTags()
        else runBlocking { gelbooruApi!!.getTags(tagString) } ?: super.getTags()
    }

    /**
     * @return this method will return default values if no api was passed in this classes contructor.
     */
    @JsonIgnore
    override fun getTags(): List<Tag> = _tags
}