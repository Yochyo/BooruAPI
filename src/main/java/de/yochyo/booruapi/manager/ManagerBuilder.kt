package de.yochyo.booruapi.manager

import de.yochyo.booruapi.api.Api

/*
PRIOTITIES:
1: ' '
2: 'OR'
3: 'NOT'
4: 'AND'
 */
object ManagerBuilder {
    fun toManager(api: Api, tagString: String, limit: Int): IManager {
        val splitByOr = tagString.split(" OR ")
        val managers = ArrayList<IManager>()
        for (s in splitByOr) managers += createExcludingManager(api, s, limit)
        if (managers.size == 1) return managers.first()
        return ManagerFolder(managers, limit)
    }

    private fun createExcludingManager(api: Api, s: String, limit: Int): IManager {
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


    private fun createManager(api: Api, s: String, limit: Int): IManager {
        val splitByAnd = s.split(" AND ")
        println(splitByAnd)
        return if (splitByAnd.size == 1) Manager(api, s.split(" ").toTypedArray(), limit)
        else ManagerBypassApi(api, splitByAnd.joinToString(" ") { it }.split(" "), limit)
    }

    fun toManagerFolder(managers: Collection<IManager>, limit: Int) = ManagerFolder(managers, limit)
}