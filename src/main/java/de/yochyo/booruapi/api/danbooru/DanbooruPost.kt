package danbooru

import com.fasterxml.jackson.annotation.JsonProperty
import de.yochyo.booruapi.api.Post
import de.yochyo.booruapi.api.Tag
import de.yochyo.booruapi.api.danbooru.DanbooruTag
import java.util.*
import kotlin.collections.ArrayList

//TODO comments
data class DanbooruPost(
        override val id: Int,
        val createdAt: Date,
        val uploaderId: Int,
        val score: Int,
        val source: String,
        val md5: String,
        val lastCommentBumpedAt: Date?,
        override val rating: String,
        val imageWidth: Int,
        val imageHeight: Int,
        override val tagString: String,
        val isNoteLocked: Boolean,
        val favCount: Boolean,
        @JsonProperty("file_ext") val fileExtension: String,
        val lastNotedAt: Date?,
        val isRatingLocked: Boolean,
        val parentId: Int?,
        val hasChildren: Boolean,
        val approverId: Int?,
        val tagCountGeneral: Int,
        val tagCountArtist: Int,
        val tagCountCharacter: Int,
        val tagCountCopyright: Int,
        override val fileSize: Int,
        val isStatusLocked: Boolean,
        val poolString: String,
        val upScore: Int,
        val downScore: Int,
        val isPending: Boolean,
        val isFlagged: Boolean,
        val isDeleted: Boolean,
        val tagCount: Int,
        val updatedAt: Date,
        val isBanned: Boolean,
        val pixivId: Int?,
        val lastCommentedAt: Date?,
        val hasActiveChildren: Boolean,
        val bitFlags: Int,
        val tagCountMeta: Int,
        val hasLarge: Boolean,
        val hasVisibleChildren: Boolean,
        val isFavorited: Boolean,
        val tagStringGeneral: String,
        val tagStringCharacter: String,
        val tagStringCopyright: String,
        val tagStringArtist: String,
        val tagStringMeta: String,
        val fileUrl: String,
        val largeFileUrl: String,
        val previewFileUrl: String
) : Post(id, fileExtension, imageWidth, imageHeight, rating, fileSize, fileUrl, largeFileUrl, previewFileUrl, tagString) {
    private val _tags by lazy {
        ArrayList<Tag>().apply {
            val tagsGeneral = tagStringGeneral.split(" ").filter { it != "" }.map { Tag(it, DanbooruTag.typeToTypeEnum(DanbooruTag.DANBOORU_GENERAL), 0) }
            val tagsCharacter = tagStringCharacter.split(" ").filter { it != "" }.map { Tag(it, DanbooruTag.typeToTypeEnum(DanbooruTag.DANBOORU_GENERAL), 0) }
            val tagsCopyright = tagStringCopyright.split(" ").filter { it != "" }.map { Tag(it, DanbooruTag.typeToTypeEnum(DanbooruTag.DANBOORU_COPYRIGHT), 0) }
            val tagsArtist = tagStringArtist.split(" ").filter { it != "" }.map { Tag(it, DanbooruTag.typeToTypeEnum(DanbooruTag.DANBOORU_ARTIST), 0) }
            val tagsMeta = tagStringMeta.split(" ").filter { it != "" }.map { Tag(it, DanbooruTag.typeToTypeEnum(DanbooruTag.DANBOORU_META), 0) }
            this.addAll(tagsArtist)
            this.addAll(tagsCopyright)
            this.addAll(tagsCharacter)
            this.addAll(tagsGeneral)
            this.addAll(tagsMeta)
        }
    }

    override fun getTags(): List<Tag> = _tags
}


