package com.ycngmn.nobook.utils

import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder

fun fbRedirectSanitizer(link: String): String {
    try {
        var url = URL(link)

        if (url.host == "l.facebook.com" && url.path == "/l.php") {
            val params = url.query.split("&").associate {
                val (key, value) = it.split("=", limit = 2)
                key to URLDecoder.decode(value, "UTF-8")
            }
            url = URL(params["u"] ?: return link)
        }

        val params = url.query?.split("&")
            ?.filter { !it.startsWith("fbclid=") }
            ?.joinToString("&") { param ->
                val (key, value) = param.split("=", limit = 2)
                "$key=${URLEncoder.encode(value, "UTF-8")}"
            }

        return buildString {
            append("${url.protocol}://${url.host}")
            if (url.port != -1 && url.port != url.defaultPort) append(":${url.port}")
            append(url.path)
            if (!params.isNullOrBlank()) append("?").append(params)
        }
    } catch (_: Exception) {
        return link
    }
}