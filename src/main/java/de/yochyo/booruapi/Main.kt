import de.yochyo.booruapi.api.danbooru.DanbooruApi
import de.yochyo.booruapi.manager.ManagerBuilder
import kotlinx.coroutines.runBlocking

fun main() {
    val api = DanbooruApi("https://danbooru.donmai.us/")
    runBlocking {
//        api.login("Yochyo", "A7lajIGiCBQxSh7ivGRxGcjXvajhCPXq8PtlM-O0D-o")
        val m = ManagerBuilder.createManager(api, "dress", 2)
//        m.downloadNextPage()
//        println(api.getTagAutoCompletion("elsw", 50))
        //    println(api.getTags("genshin_impact"))
        println(api.getPosts(1, "", 1)!!.first().getTags())
    }
}
