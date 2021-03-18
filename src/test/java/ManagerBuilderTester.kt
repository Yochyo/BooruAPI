import de.yochyo.booruapi.api.danbooru.DanbooruApi
import de.yochyo.booruapi.manager.Manager
import de.yochyo.booruapi.manager.ManagerBuilder
import de.yochyo.booruapi.manager.ManagerNOT
import de.yochyo.booruapi.manager.ManagerOR
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertTrue

class ManagerBuilderTester {
    val api = DanbooruApi("https://danbooru.donmai.us/")
    fun getManager(s: String) = ManagerBuilder.createManager(api, s, 1)

    @Test
    fun createsNormalManager() {
        val manager = getManager("*")
        runBlocking { manager.downloadNextPages(1) }
        assertTrue { manager is Manager }
    }

    @Test
    fun createsExcludingManager() {
        val manager = getManager("test NOT(hallo) NOT(test dsa) banana NOT(3)")
        assertTrue { manager is ManagerNOT }
    }

    @Test
    fun createsFolderManager() {
        val manager = getManager("test OR bana")
        assertTrue { manager is ManagerOR }
    }

    @Test
    fun notRegexTest() {
        val regex = "NOT\\(((?!\\)[ \$]).)*\\)".toRegex()
        val a = "test NOT(hallo) dsa".contains(regex)
        val a1 = "test NOT(hallo) dsa".contains(regex)
        val a2 = "test NOT(hallo)".contains(regex)
        val a3 = "NOT(hallo) dsa".contains(regex)
        val a4 = "NOT(hallo dsadsa dsa".contains(regex)
    }

    @Test
    fun managerNOT() {
        val m1 = ManagerBuilder.createManager(api, "NOT(a) NOT(b) NOT(c) *", 10)
        println()
    }

    @Test
    fun managerId() {
        val m1 = ManagerBuilder.createManager(api, "id:>1 id:>2 * NOT(2) OR id:>1", 10)
        println()
    }
}