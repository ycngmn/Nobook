package com.ycngmn.nobook.utils

import android.content.Context
import android.webkit.WebView
import com.ycngmn.nobook.R

object AntiReloadInjector {

    private val TARGET_HOSTS = listOf(
        "facebook.com",
        "m.facebook.com",
        "www.facebook.com",
        "web.facebook.com",
        "fbsbx.com",
        "www.fbsbx.com"
    )

    fun isFacebookHost(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        return TARGET_HOSTS.any { host == it || host.endsWith(".$it") }
    }

    private var cachedScript: String? = null

    private fun loadScript(context: Context): String {
        return cachedScript ?: context.resources
            .openRawResource(R.raw.anti_reload)
            .bufferedReader()
            .use { it.readText() }
            .also { cachedScript = it }
    }

    fun inject(context: Context, webView: WebView, host: String?) {
        if (!isFacebookHost(host)) return
        webView.evaluateJavascript(loadScript(context), null)
    }
}
