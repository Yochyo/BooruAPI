package de.yochyo.booruapi.api.pixiv

import de.yochyo.booruapi.api.Tag
import de.yochyo.booruapi.api.TagType

class PixivTag(override val name: String, override val tagType: TagType = TagType.UNKNOWN, val id: Int) :
    Tag(name, tagType, 0) {
}