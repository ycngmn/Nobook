package com.ycngmn.nobook.utils

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get

data class Script(
    val condition: Boolean,
    val scriptRes: Int,
    val cdnUrl: String
)

val httpClient = HttpClient(OkHttp)
suspend fun fetchScripts(scripts: List<Script>, context: Context): String {
    return buildString {
        scripts.filter { it.condition }.forEach { script ->
            val content =
                try {
                    httpClient.get(script.cdnUrl).body<String>()
                } catch (_: Exception) {
                    context.resources.openRawResource(script.scriptRes)
                        .bufferedReader()
                        .use { it.readText() }
                }
            append(content)
        }
    }
}