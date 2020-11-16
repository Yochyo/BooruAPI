package de.yochyo.booruapi.api.gelbooru

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import de.yochyo.booruapi.api.Tag
import de.yochyo.booruapi.api.TagType
import de.yochyo.booruapi.deserializers.NumericBooleanDeserializer

//TODO comments
data class GelbooruTag(
        val id: Int,
        @JsonProperty("tag") override val name: String,
        override val count: Int,
        @JsonProperty("type") val typeString: String,
        @JsonDeserialize(using = NumericBooleanDeserializer::class) val ambiguous: Boolean = false
) : Tag(name, typeStringToEnum(typeString), count) {
    companion object {
        const val GELBOORU_GENERAL = "tag"
        const val GELBOORU_ARTIST = "artist"
        const val GELBOORU_COPYRIGHT = "copyright"
        const val GELBOORU_META = "metadata"
        const val GELBOORU_CHARACTER = "character"
        const val GELBOORU_UNKNOWN = "unknown"

        fun typeStringToEnum(type: String): TagType {
            return when (type) {
                GELBOORU_COPYRIGHT -> TagType.COPYRIGHT
                GELBOORU_META -> TagType.META
                GELBOORU_CHARACTER -> TagType.CHARACTER
                GELBOORU_GENERAL -> TagType.GENERAL
                GELBOORU_ARTIST -> TagType.ARTIST
                else -> TagType.UNKNOWN
            }
        }
    }
}