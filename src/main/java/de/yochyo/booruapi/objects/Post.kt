package de.yochyo.booruapi.objects

import de.yochyo.booruapi.api.IApi

open class Post(open val id: Int, open val extention: String, open val width: Int, open val height: Int, open val rating: String, open val fileSize: Int,
                open val fileURL: String, open val fileSampleURL: String, open val filePreviewURL: String,
                open val tags: List<Tag>, open val tagString: String, open val api: IApi): Comparable<Post> {

    override fun toString() = "[ID: $id] [${width}x$height]\n$fileURL\n$fileSampleURL\n$filePreviewURL\n{$tagString}"

    override fun compareTo(other: Post): Int {
        return id.compareTo(other.id)
    }
}