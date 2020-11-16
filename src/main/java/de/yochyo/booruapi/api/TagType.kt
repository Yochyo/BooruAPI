package de.yochyo.booruapi.api

enum class TagType(val value: Int) {
    ARTIST(0), COPYRIGHT(1), CHARACTER(2), GENERAL(3), META(4), UNKNOWN(99);

    companion object {
        fun valueOf(value: Int): TagType {
            return when (value) {
                0 -> ARTIST
                1 -> COPYRIGHT
                2 -> CHARACTER
                3 -> GENERAL
                4 -> META
                else -> UNKNOWN
            }
        }
    }
}