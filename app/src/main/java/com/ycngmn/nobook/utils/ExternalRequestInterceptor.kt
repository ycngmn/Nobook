package com.ycngmn.nobook.utils

import android.content.Context
import android.content.Intent
import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.web.WebViewNavigator

class ExternalRequestInterceptor(
    private val context: Context,
    private val toggleMessenger: () -> Unit
) : RequestInterceptor {

    override fun onInterceptUrlRequest(
        request: WebRequest,
        navigator: WebViewNavigator
    ): WebRequestInterceptResult {

        val internalLinkRegex =
            Regex("https?://(?!(l|lm)\\.facebook\\.com)([^/]+\\.)?(facebook\\.com|messenger\\.com)/.*")

        return if (internalLinkRegex.containsMatchIn(request.url) && request.isForMainFrame) {
            WebRequestInterceptResult.Allow
        } else {
            openInBrowser(request.url)
            WebRequestInterceptResult.Reject
        }
    }

    private fun openInBrowser(url: String) {

        val cleanUrl = fbRedirectSanitizer(url)

        try {
            val intent = Intent.parseUri(cleanUrl, Intent.URI_INTENT_SCHEME)
            context.startActivity(intent)
        } catch (_: Exception) {
            // if fails to open in messenger app
            if (url.contains("fb-messenger://threads"))
                toggleMessenger()
        }
    }
}