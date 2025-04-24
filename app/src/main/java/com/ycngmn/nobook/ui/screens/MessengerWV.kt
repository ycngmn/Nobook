package com.ycngmn.nobook.ui.screens

import android.app.Activity
import android.view.View
import android.webkit.CookieManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.ycngmn.nobook.ui.components.NetworkErrorDialog
import com.ycngmn.nobook.ui.components.PopLoadingAnimation
import com.ycngmn.nobook.utils.fileChooserWebViewParams
import com.ycngmn.nobook.utils.jsBridge.DownloadBridge
import com.ycngmn.nobook.utils.jsBridge.ThemeChange

@Composable
fun MessengerWebView() {

    val context = LocalContext.current
    val window = (context as Activity).window

    val state = rememberWebViewState("https://facebook.com/messages")
    val navigator = rememberWebViewNavigator()

    val isLoading = remember { mutableStateOf(true) }
    val isError = state.errorsForCurrentRequest.lastOrNull()?.isFromMainFrame == true

    LaunchedEffect(state.loadingState) {
        if (state.loadingState is LoadingState.Finished && !isError) {
            isLoading.value = false
        }
    }

    state.webSettings.apply {

        customUserAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36"

        isJavaScriptEnabled = true
        supportZoom = false

        androidWebSettings.apply {
            domStorageEnabled = true
            hideDefaultVideoPoster = true
            mediaPlaybackRequiresUserGesture = false
        }
    }

    if (isError && isLoading.value) {
        NetworkErrorDialog(context)
        return
    }

    if (isLoading.value)
        PopLoadingAnimation()

    Box {

        WebView(
            modifier = Modifier.fillMaxSize().imePadding(),
            state = state,
            navigator = navigator,
            platformWebViewParams = fileChooserWebViewParams(), // to select media from storage.
            onCreated = { webView ->
                // Save cookies to retain logins and stuff.
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(webView, true)

                webView.apply {
                    addJavascriptInterface(ThemeChange(window), "ThemeBridge")
                    addJavascriptInterface(DownloadBridge(context), "DownloadBridge")
                    // Hide scrollbars
                    overScrollMode = View.OVER_SCROLL_NEVER
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                }
            }
        )
    }
}



