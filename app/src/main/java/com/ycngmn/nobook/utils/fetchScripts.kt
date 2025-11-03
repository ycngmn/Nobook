package com.ycngmn.nobook.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode


const val SCRIPT_SRC = "https://raw.githubusercontent.com/ycngmn/Nobook/refs/heads/main/app/src/main/res/raw/"

data class Script(
    val condition: Boolean,
    val scriptRes: Int,
    val scriptTitle: String
)

suspend fun fetchScripts(
    scripts: List<Script>,
    defaultResource: (Int) -> String
): String {

    val httpClient = HttpClient(OkHttp)
    return buildString {
        scripts.filter { it.condition }.forEach { script ->
            val content = runCatching {
                val res = httpClient.get(SCRIPT_SRC + script.scriptTitle)
                if (res.status == HttpStatusCode.OK) res.body() as String
                else throw Exception("Couldn't fetch remote resource")
            }.getOrElse { defaultResource(script.scriptRes) }
            append(content)
        }
    }
}
