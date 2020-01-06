package de.yochyo.booruapi.objects

open class Post(open val id: Int, open val extention: String, open val width: Int, open val height: Int, open val rating: String, open val fileSize: Int,
                open val fileURL: String, open val fileSampleURL: String, open val filePreviewURL: String,
                open val tags: List<Tag>) {

    override fun toString() = "[ID: $id] [${width}x$height]\n$fileURL\n$fileSampleURL\n$filePreviewURL\n{${tags.joinToString { it.toString() }}}"
}