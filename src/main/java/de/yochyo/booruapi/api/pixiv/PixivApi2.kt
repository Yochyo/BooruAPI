package de.yochyo.booruapi.api.pixiv

import de.yochyo.booruapi.api.IBooruApi
import de.yochyo.booruapi.api.Post
import de.yochyo.booruapi.api.Tag
import de.yochyo.booruapi.api.TagType
import de.yochyo.pixiv_api.PixivApi
import de.yochyo.pixiv_api.response_types.PixivIllustSearch
import de.yochyo.pixiv_api.response_types.PixivUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.*
import kotlin.collections.HashMap


class PixivApi2 : IBooruApi {
    override val host: String = "https://pixiv.net/"
    private var refreshToken: String? = null

    val api = PixivApi().apply { setLanguage("en-us") }

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

    override suspend fun getTag(name: String): PixivTag {
        val tag = try {
            getUserTag(name)
        } catch (e: Exception) {
            null
        }
        return tag ?: PixivTag(name, TagType.UNKNOWN, 0)
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

    override suspend fun getTagAutoCompletion(begin: String, limit: Int): List<Tag>? {
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
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


    private suspend fun getIllustsFirstPage(tags: String): PixivIllustSearch? {
        try {
            if (tags == "*") return api.getNewIllusts()
            val illusts = GlobalScope.async(Dispatchers.IO) { api.searchIllust(tags) }
            val userId = getUserTag(tags)?.id ?: -1

            return if (userId == -1) illusts.await()
            else api.getUserIllusts(userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private val searches = HashMap<String, PixivIllustSearchList>()
    override suspend fun getPosts(page: Int, tags: String, limit: Int): List<Post>? {
        var search = searches[tags]
        if (search == null) {
            val illusts = getIllustsFirstPage(tags) ?: return null
            search = PixivIllustSearchList(api, illusts)
            searches[tags] = search
        }
        return search.get(page)?.toPosts()
    }

    fun PixivIllustSearch.toPosts() = this.illusts.map { PixivPost(it) }

    private fun pixivUserToTag(user: PixivUser): PixivTag {
        return PixivTag(user.name, TagType.ARTIST, user.id)
    }

    private fun pixivTagToTag(tag: de.yochyo.pixiv_api.response_types.PixivTag): PixivTag? {
        val name = tag.translatedName ?: tag.name ?: return null
//        TODO()//tagtype
        return PixivTag(name, TagType.GENERAL, -1)
    }

    override fun getHeaders(): Map<String, String> {
        return api.additionalHeaders
    }
}