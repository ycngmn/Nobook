package com.ycngmn.nobook.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.web.WebViewNavigator

class ExternalRequestInterceptor(
    private val context: Context
) : RequestInterceptor {

    override fun onInterceptUrlRequest(
        request: WebRequest,
        navigator: WebViewNavigator
    ): WebRequestInterceptResult {

        if (request.isRedirect) {
            return WebRequestInterceptResult.Reject
        }

        val internalLinkRegex = Regex("https?://(www\\.)?facebook\\.com/.*")

        return if (internalLinkRegex.containsMatchIn(request.url)) {
            WebRequestInterceptResult.Allow
        } else {
            openInBrowser(request.url)
            WebRequestInterceptResult.Reject
        }
    }

    private fun openInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    }
}