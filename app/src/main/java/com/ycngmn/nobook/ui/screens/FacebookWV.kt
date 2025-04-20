package com.ycngmn.nobook.ui.screens

import android.view.View
import android.webkit.CookieManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.ycngmn.nobook.utils.ExternalRequestInterceptor
import com.ycngmn.nobook.utils.sponsoredAdBlockerScript
import com.ycngmn.nobook.utils.styling.HIDE_OPEN_WITH_APP_BANNER_SCRIPT
import com.ycngmn.nobook.utils.styling.enhanceLoadingOverlayScript
import com.ycngmn.nobook.utils.styling.holdEffectScript
import com.ycngmn.nobook.utils.styling.removeBottomPaddingScript
import com.ycngmn.nobook.utils.zoomDisableScript

@Composable
fun FacebookWebView() {

    val context = LocalContext.current
    val state = rememberWebViewState("https://m.facebook.com")
    val navigator = rememberWebViewNavigator(
        requestInterceptor = ExternalRequestInterceptor(context = context))
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(state.loadingState) {
        if (state.loadingState is LoadingState.Finished) {
            navigator.evaluateJavaScript(
                HIDE_OPEN_WITH_APP_BANNER_SCRIPT +
                zoomDisableScript +
                sponsoredAdBlockerScript +
                holdEffectScript +
                enhanceLoadingOverlayScript +
                removeBottomPaddingScript
            )
            isLoading = false
        }
    }

    if (isLoading)
        SplashLoading(state.loadingState)

    state.webSettings.apply {
        isJavaScriptEnabled = true
        supportZoom = false

        androidWebSettings.apply {
            hideDefaultVideoPoster = true
            allowFileAccess = true
            mediaPlaybackRequiresUserGesture = false
        }
    }

    WebView(
        modifier = Modifier.fillMaxSize(),
        state = state,
        navigator = navigator,
        onCreated = { webView ->
            // Save cookies to retain logins and stuff.
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true)

            webView.apply {
                // Hide scrollbars
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
            }
        }
    )
}