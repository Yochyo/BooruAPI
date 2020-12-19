package de.yochyo.booruapi.api.pixiv

import de.yochyo.booruapi.api.IBooruApi
import de.yochyo.booruapi.api.Post
import de.yochyo.booruapi.api.Tag
import de.yochyo.booruapi.api.TagType
import de.yochyo.pixiv_api.PixivApi
import de.yochyo.pixiv_api.request_types.PixivParams
import de.yochyo.pixiv_api.response_types.PixivIllustSearch
import de.yochyo.pixiv_api.response_types.PixivUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.*
import kotlin.collections.HashMap

class PixivApi2 : IBooruApi {
    override val host: String = ""
    private var refreshToken: String? = null

    private val api = PixivApi()

    /**
     * password is refreshToken if username is empty
     */
    override suspend fun login(username: String, password: String): Boolean {
        return try {
            if (username == "" && password != "") {
                api.login(password)
            } else {
                val client = api.login(username, password)
                refreshToken = client.refreshToken
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getTag(name: String): PixivTag? {
        val user = GlobalScope.async { getUserTag(name) }
        val tag = GlobalScope.async { getIllustTag(name) }
        return user.await() ?: tag.await()
    }

    suspend fun getUserTag(name: String): PixivTag? {
        try {
            val users = api.searchUser(name)
            val user = users.userPreviews.firstOrNull { it.user.name.equals(name, true) }
            if (user != null) return pixivUserToTag(user.user)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun getIllustTag(name: String): PixivTag? {
        try {
            val tags = api.searchAutoCompletion(name)
            val tag = tags.tags.firstOrNull { it.name.equals(name, true) || it.translatedName.equals(name, true) }
            if (tag != null) return pixivTagToTag(tag)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override suspend fun getTagAutoCompletion(begin: String, limit: Int): List<Tag>? {
        val users = GlobalScope.async {
            try {
                api.searchUser(begin)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        val tags = GlobalScope.async {
            try {
                api.searchAutoCompletion(begin)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        return mergeTagsBySimilarity(
            begin,
            users.await()?.userPreviews?.map { pixivUserToTag(it.user) } ?: emptyList(),
            tags.await()?.tags?.mapNotNull { pixivTagToTag(it) } ?: emptyList()
        )
    }

    private fun mergeTagsBySimilarity(base: String, vararg tags: List<PixivTag>): List<PixivTag> {
        val list = LinkedList<PixivTag>()
        for (tagList in tags) list += tagList
        return sortTagsBySimilarity(base, list)
    }

    private fun sortTagsBySimilarity(base: String, tags: List<PixivTag>): List<PixivTag> {
        fun levenshtein(tag: String): Int {
            val lhsLength = base.length
            val rhsLength = tag.length

            var cost = IntArray(lhsLength + 1) { it }
            var newCost = IntArray(lhsLength + 1) { 0 }

            for (i in 1..rhsLength) {
                newCost[0] = i

                for (j in 1..lhsLength) {
                    val editCost = if (base[j - 1] == tag[i - 1]) 0 else 1

                    val costReplace = cost[j - 1] + editCost
                    val costInsert = cost[j] + 1
                    val costDelete = newCost[j - 1] + 1

                    newCost[j] = minOf(costInsert, costDelete, costReplace)
                }

                val swap = cost
                cost = newCost
                newCost = swap
            }

            return cost[lhsLength]
        }
        return tags.sortedBy { levenshtein(it.name) }
    }

    private val cachedTags = HashMap<String, Int>() //tag, userId
    override suspend fun getPosts(page: Int, tags: String, limit: Int): List<Post>? {
        val userId = cachedTags[tags] ?: getUserTag(tags)?.id ?: -1
        cachedTags[tags] = userId

        return if (userId == -1) api.searchIllust(tags, PixivParams(offset = (page - 1) * 30)).toPosts()
        else api.getUserIllusts(userId, PixivParams(offset = (page - 1) * 30)).toPosts()
    }

    fun PixivIllustSearch.toPosts() = this.illusts.map { PixivPost(it) }

    private fun pixivUserToTag(user: PixivUser): PixivTag {
        return PixivTag(user.name, TagType.ARTIST, user.id)
    }

    private fun pixivTagToTag(tag: de.yochyo.pixiv_api.response_types.PixivTag): PixivTag? {
        val name = tag.translatedName ?: tag.name ?: return null
//        TODO()//tagtype
        return PixivTag(name, TagType.UNKNOWN, -1)
    }
}