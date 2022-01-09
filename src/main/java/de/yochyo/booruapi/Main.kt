import de.yochyo.booruapi.api.gelbooru.GelbooruApi
import de.yochyo.booruapi.manager.ManagerBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun main() {
    val api = GelbooruApi("https://gelbooru.com/")
    val m = ManagerBuilder.createManager(api, "ano_(gccx8784)", 50)
    runBlocking {
        m.downloadNextPage()
        m.downloadNextPage()
        m.downloadNextPage()
        //    println(api.getTags("genshin_impact"))
        //   println(api.getTagAutoCompletion("genshi", 10))
        //  println(api.getTag("genshin_impact") as Tag)
    }
    println()
}

val mu = Mutex()
suspend fun test(i: Int) {
    mu.withLock {
        if (i < 100) test(i + 1)
    }
}
