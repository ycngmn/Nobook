package com.ycngmn.nobook.ui.screens

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebSettings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.ycngmn.nobook.utils.fixWebViewVideoPosterScript
import com.ycngmn.nobook.utils.hideOpenWithAppBannerScript
import com.ycngmn.nobook.utils.sponsoredAdBlockerScript
import com.ycngmn.nobook.utils.zoomDisableScript


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FacebookWebView() {

    val state = rememberWebViewState("https://m.facebook.com")
    val navigator = rememberWebViewNavigator()
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(state.loadingState) {
        if (state.loadingState is LoadingState.Finished) {
            navigator.evaluateJavaScript(
                hideOpenWithAppBannerScript +
                zoomDisableScript +
                fixWebViewVideoPosterScript +
                sponsoredAdBlockerScript
            )
            isLoading = false
        }
    }

    if (isLoading)
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())

    WebView(
        modifier = Modifier.fillMaxSize(),
        state = state,
        navigator = navigator,
        onCreated = { webView ->
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                // to support video auto-play.
                mediaPlaybackRequiresUserGesture = false

                // Disable zooming
                builtInZoomControls = false
                displayZoomControls = false
                loadWithOverviewMode = false
                useWideViewPort = false
                setSupportZoom(false)
            }

            webView.apply {
                // Hide scrollbars
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
            }

            // Save cookies to retain logins.
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true)

        }
    )
}