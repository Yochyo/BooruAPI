import de.yochyo.booruapi.api.danbooru.DanbooruApi
import de.yochyo.booruapi.manager.ManagerBuilder
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun main() {
    val api = DanbooruApi("https://danbooru.donmai.us/")
    val m = ManagerBuilder.createManager(api, "FOREACH( 0 A) FOREACH(B) 1girl OR 2girl NOT(test) THEN FOREACH(C) 3girl", 1)
    println()
}

val mu = Mutex()
suspend fun test(i: Int) {
    mu.withLock {
        if (i < 100) test(i + 1)
    }
}