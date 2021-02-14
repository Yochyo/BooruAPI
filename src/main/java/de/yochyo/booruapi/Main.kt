import de.yochyo.booruapi.api.danbooru.DanbooruApi
import de.yochyo.booruapi.manager.ManagerBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun main() {
    val api = DanbooruApi("https://danbooru.donmai.us/")
    //runBlocking { api.login("Yochyo", "A7lajIGiCBQxSh7ivGRxGcjXvajhCPXq8PtlM-O0D-o") }
    //  val api = MoebooruApi("https://konachan.com/")
    val m = ManagerBuilder.createManager(api, "* id:>1", 50)
    runBlocking { println(m.downloadNextPage()?.size) }
    runBlocking { println(m.downloadNextPage()?.size) }
    println()

}

val mu = Mutex()
suspend fun test(i: Int) {
    mu.withLock {
        if (i < 100) test(i + 1)
    }
}