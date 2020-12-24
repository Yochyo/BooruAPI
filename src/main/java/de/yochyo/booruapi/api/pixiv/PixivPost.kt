package de.yochyo.booruapi.api.pixiv

import de.yochyo.booruapi.api.Post
import de.yochyo.booruapi.api.TagType
import de.yochyo.pixiv_api.response_types.PixivIllust
import java.util.*

//TODO("extension")
class PixivPost(val illust: PixivIllust) : Post(
    illust.id, "png",
    illust.width, illust.height, illust.sanityLevel.toString(), -1,
    illust.metaSinglePage.originalImageUrl ?: illust.metaPages.firstOrNull()?.imageUrls?.original ?: illust.imageUrls.large ?: illust.imageUrls.medium,
    illust.imageUrls.large ?: illust.imageUrls.medium, illust.imageUrls.medium,
    illust.tags.mapNotNull { it.translatedName ?: it.name }.joinToString(" ")
) {
    private val tags by lazy {
        val tags = LinkedList<PixivTag>()
        tags += PixivTag(illust.user.name, TagType.ARTIST, illust.user.id)
        tags += illust.tags.mapNotNull { it.translatedName ?: it.name }.map { PixivTag(it, TagType.UNKNOWN, -1) }
        tags
    }

    override fun getTags(): List<PixivTag> {
        return tags
    }
}