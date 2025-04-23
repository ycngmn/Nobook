package com.ycngmn.nobook.utils

import android.content.Context
import android.content.Intent
import android.util.Log.wtf
import android.widget.Toast
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
        wtf("wtf", "onInterceptUrlRequest: ${request.url}" )

        val internalLinkRegex = Regex("https?://([a-zA-Z0-9-]+\\.)?facebook\\.com/.*")

        return if (internalLinkRegex.containsMatchIn(request.url) && request.isForMainFrame) {
            WebRequestInterceptResult.Allow
        } else {
            openInBrowser(request.url)
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