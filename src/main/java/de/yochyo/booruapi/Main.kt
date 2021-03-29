import de.yochyo.booruapi.api.danbooru.DanbooruApi
import de.yochyo.booruapi.manager.ManagerBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun main() {
    val api = DanbooruApi("https://danbooru.donmai.us/")
    val m = ManagerBuilder.createManager(api, "EACH(ass) 1girl OR aisaka_taiga", 30)
    runBlocking {
        m.downloadNextPage()
    }
    println()
}

val mu = Mutex()
suspend fun test(i: Int) {
    mu.withLock {
        if (i < 100) test(i + 1)
    }
}