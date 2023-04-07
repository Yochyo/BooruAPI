import de.yochyo.booruapi.api.gelbooru_beta.GelbooruBetaApi
import de.yochyo.booruapi.manager.ManagerBuilder
import kotlinx.coroutines.runBlocking

fun main() {
    val api = GelbooruBetaApi("https://rule34.xxx/")
//    val api = DanbooruApi("https://rule34.xxx/")
    val m = ManagerBuilder.createManager(api, "dress", 50)
    runBlocking {
//        m.downloadNextPage()
//        println(api.getTagAutoCompletion("elsw", 50))
        //    println(api.getTags("genshin_impact"))
        println(api.getPosts(1, "", 1)!!.first().getTags())
    }
}
