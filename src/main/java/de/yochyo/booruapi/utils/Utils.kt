package de.yochyo.booruapi.utils

import de.yochyo.booruapi.objects.Tag
import java.net.URLEncoder
import java.security.MessageDigest

fun passwordToHash(password: String): String {
    val byteArray = "choujin-steiner--$password--".toByteArray(charset = Charsets.UTF_8)
    val digest = MessageDigest.getInstance("SHA-1")
    digest.update(byteArray)
    val digestBytes = digest.digest()
    val digestStr = StringBuilder()
    for (b in digestBytes)
        digestStr.append(String.format("%02x", b))
    return digestStr.toString()
}

fun parseURL(url: String): String {
    val b = StringBuilder()
    if (!url.startsWith("http"))
        b.append("https://")
    b.append(url)
    if (!url.endsWith("/"))
        b.append("/")
    return b.toString()
}

fun isSpecialTag(name: String): Boolean {
    return name == "*" || name.startsWith("height") || name.startsWith("width") || name.startsWith("order") || name.startsWith("rating") || name.contains(" ")
}

fun getCorrectTagType(tagName: String, type: Int): Int {
    return if (type in 0..1 || type in 3..5) type
    else if (isSpecialTag(tagName)) Tag.SPECIAL
    else Tag.UNKNOWN
}

fun parseUFT8(urlStr: String): String {
    return URLEncoder.encode(urlStr, "UTF-8")
}