package de.yochyo.booruapi.api.pixiv

import de.yochyo.booruapi.api.Post
import de.yochyo.pixiv_api.response_types.PixivIllust

//TODO("extension")
class PixivPost(val illust: PixivIllust) :
    Post(illust.id,
        "png", illust.width, illust.height, illust.sanityLevel.toString(), -1,
        illust.imageUrls.large ?: illust.imageUrls.medium, illust.imageUrls.medium,
        illust.imageUrls.squareMedium, illust.tags.mapNotNull { it.translatedName ?: it.name }.joinToString(" ")
    )