package de.yochyo.booruapi.manager

import de.yochyo.booruapi.api.IBooruApi

/*
PRIOTITIES:
1: ' '
2: 'OR'
3: 'NOT'
4: 'AND'
 */
object ManagerBuilder {
    fun createManager(api: IBooruApi, tagString: String, limit: Int): IManager {
        return BufferedManager(
            when {
                tagString.contains(" OR ") -> BufferedManager(ManagerOR(tagString.split(" OR ").map { createManager(api, it, limit) }, limit))
                tagString.contains("NOT\\(((?!\\)[ \$]).)*\\)".toRegex()) -> "NOT\\(((?!\\)[ \$]).)*\\)".toRegex().let { regex ->
                    BufferedManager(ManagerNOT(
                        createManager(api, regex.replace(tagString, "").split(" ").filter { it != "" }.joinToString(" ") { it }, limit),
                        regex.findAll(tagString).toList().map { it.value }.map { it.substring(4, it.length - 1).filter { char -> char != ' ' } })
                    )
                }
                /*
                tagString.contains(" AND ") -> BufferedManager(
                    ManagerAND(
                        createManager(api, tagString.split(" AND ").first(), limit),
                        tagString.substringAfter(" AND ").split(" AND "), limit
                    )
                )
                 */
                else -> Manager(api, tagString, limit)
            }
        )
    }

    fun toManagerOR(managers: Collection<IManager>, limit: Int) = ManagerOR(managers, limit)
}