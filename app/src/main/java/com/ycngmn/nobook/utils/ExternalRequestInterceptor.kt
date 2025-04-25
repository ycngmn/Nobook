package com.ycngmn.nobook.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
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

        val internalLinkRegex = Regex("https?://(www\\.|m\\.)facebook\\.com/.*")
        val url = request.url

        return if (internalLinkRegex.containsMatchIn(url) && request.isForMainFrame) {
            WebRequestInterceptResult.Allow
        } else {
            val sanitizedUrl = if (url.contains("l.facebook.com"))
                url.toUri().query?.substringAfter("u=")?.substringBeforeLast("?fbclid=")
                    ?.substringBeforeLast("&h=") ?: url else url
            openInBrowser(sanitizedUrl)
            WebRequestInterceptResult.Reject
        }
    }

    private fun openInBrowser(url: String) {
        try {
            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            context.startActivity(intent)
        } catch (_: Exception) {
            // if fails to open in messenger app
            if (url.contains("fb-messenger://threads")) {
                Toast.makeText(context, "Opening Messenger...", Toast.LENGTH_SHORT).show()
                toggleMessenger()
            }
        }
    }
}