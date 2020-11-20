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
        var gelbooruApi: GelbooruApi? = null,
) : Post(
        id, imageName.extension(), width, height, rating, -1, fileUrl,
        getSampleUrl(fileUrl, directory, hash),
        getPreviewUrl(fileUrl, directory, hash), tagString) {
    private val _tags by lazy {
        if (gelbooruApi == null) super.getTags()
        else runBlocking { gelbooruApi!!.getTags(tagString) } ?: super.getTags()
    }

    /**
     * @return this method will return default values if no api was passed in this classes contructor.
     */
    @JsonIgnore
    override fun getTags(): List<Tag> = _tags


    companion object {
        private fun getSampleUrl(fileUrl: String, directory: String, hash: String): String {
            return "https://img1.${URL(fileUrl).host.substringAfter(".")}/samples/$directory/sample_$hash.jpg"
        }

        private fun getPreviewUrl(fileUrl: String, directory: String, hash: String): String {
            return "https://img1.${URL(fileUrl).host.substringAfter(".")}/thumbnails/$directory/thumbnail_$hash.jpg"
        }
    }
    /*
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
     */
}