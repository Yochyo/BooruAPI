package de.yochyo.booruapi.api

import de.yochyo.booruapi.objects.Post
import de.yochyo.booruapi.objects.Tag
import org.json.JSONObject

class DanbooruApi(url: String) : Api(url) {
    override fun urlGetTag(name: String) = "${url}tags.json?search[name_matches]=$name"
    override fun urlGetTags(beginSequence: String): String {
        return "${url}tags.json?search[name_matches]=$beginSequence*&limit=$DEFAULT_TAG_LIMIT&search[order]=count"
    }

    override fun urlGetPosts(page: Int, tags: Array<String>, limit: Int): String {
        return "${url}posts.json?limit=$limit&page=$page&login=$username&password_hash=$passwordHash"
    }

    override suspend fun getPostFromJson(json: JSONObject): Post? {
        return try {
            with(json) {
                val tagsGeneral = json.getString("tag_string_general").split(" ").filter { it != "" }.map { Tag(it, Tag.GENERAL) }
                val tagsCharacter = json.getString("tag_string_character").split(" ").filter { it != "" }.map { Tag(it, Tag.CHARACTER) }
                val tagsCopyright = json.getString("tag_string_copyright").split(" ").filter { it != "" }.map { Tag(it, Tag.COPYPRIGHT) }
                val tagsArtist = json.getString("tag_string_artist").split(" ").filter { it != "" }.map { Tag(it, Tag.ARTIST) }
                val tagsMeta = json.getString("tag_string_meta").split(" ").filter { it != "" }.map { Tag(it, Tag.META) }
                val tags = ArrayList<Tag>(tagsGeneral.size + tagsCharacter.size + tagsCopyright.size + tagsArtist.size + tagsMeta.size)
                tags += tagsArtist
                tags += tagsCopyright
                tags += tagsCharacter
                tags += tagsGeneral
                tags += tagsMeta
                Post(getInt("id"), getString("file_ext"), getInt("image_width"), getInt("image_height"),
                        getString("rating"), getInt("file_size"), getString("file_url"),
                        getString("large_file_url"), getString("preview_file_url"), tags)
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getTagFromJson(json: JSONObject): Tag? {
        return try {
            val name = json.getString("name")
            Tag(name, json.getInt("category"), count = json.getInt("post_count"))
        } catch (e: Exception) {
            null
        }
    }
}