package de.yochyo.booruapi.utils

import java.net.URLEncoder

fun encodeUTF8(urlStr: String): String {
    return URLEncoder.encode(urlStr, "UTF-8")
}

fun String.extension(): String = this.substringAfterLast(".")