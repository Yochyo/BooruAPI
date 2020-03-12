package de.yochyo.booruapi.manager

import de.yochyo.booruapi.api.IApi

/*
PRIOTITIES:
1: ' '
2: 'OR'
3: 'NOT'
4: 'AND'
 */
object ManagerBuilder {
    fun toManager(api: IApi, tagString: String, limit: Int): IManager {
        val result: IManager
        val splitByOr = tagString.split(" OR ")
        val managers = ArrayList<IManager>()
        for (s in splitByOr) managers += createExcludingManager(api, s, limit)
        result = if (managers.size == 1) managers.first()
        else ManagerFolder(managers, limit)
        return BufferedManager(result)
    }

    private fun createExcludingManager(api: IApi, s: String, limit: Int): IManager {
        fun extractNotStatements(s: String): Pair<Collection<String>, String> { //NotStatements, StatementWithoutNots
            val split = s.split(" ")
            val withoutNots = ArrayList<String>()
            val nots = ArrayList<String>()

            var isInNotStatement = false
            for (i in split.indices) {
                val str = split[i]
                if (!isInNotStatement) {
                    if (str.startsWith("NOT(")) isInNotStatement = true
                    else withoutNots += str
                } else {
                    if (str == ")") isInNotStatement = false
                    else nots += str
                }
            }
            return Pair(nots, withoutNots.joinToString(" ") { it })
        }

        val result = extractNotStatements(s)
        val childManager = createManager(api, result.second, limit)
        return if (result.first.isEmpty()) childManager
        else ManagerExcluding(childManager, result.first)
    }


    private fun createManager(api: IApi, s: String, limit: Int): IManager {
        val splitByAnd = s.split(" AND ")
        println(splitByAnd)
        return if (splitByAnd.size == 1) Manager(api, s.split(" ").toTypedArray(), limit)
        else ManagerBypassApi(api, splitByAnd.joinToString(" ") { it }.split(" "), limit)
    }

    fun toManagerFolder(managers: Collection<IManager>, limit: Int) = ManagerFolder(managers, limit)
}