package com.ycngmn.nobook.ui.screens

import android.app.Activity
import android.view.View
import android.webkit.CookieManager
import android.widget.Toast
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
import com.ycngmn.nobook.R
import com.ycngmn.nobook.ui.components.NetworkErrorDialog
import com.ycngmn.nobook.utils.ExternalRequestInterceptor
import com.ycngmn.nobook.utils.fileChooserWebViewParams
import com.ycngmn.nobook.utils.jsBridge.DownloadBridge
import com.ycngmn.nobook.utils.jsBridge.ThemeChange

@Composable
fun FacebookWebView(onOpenMessenger: () -> Unit) {
    val context = LocalContext.current
    val window = (context as Activity).window

    val state = rememberWebViewState("https://m.facebook.com")
    val navigator = rememberWebViewNavigator(
        requestInterceptor = ExternalRequestInterceptor(context = context) {
            onOpenMessenger()
        }
    )

    val isLoading = remember { mutableStateOf(true) }
    val isError = state.errorsForCurrentRequest.lastOrNull()?.isFromMainFrame == true

    LaunchedEffect(state.loadingState) {
        if (state.loadingState is LoadingState.Finished && !isError) {
            val script = context.resources.openRawResource(R.raw.scripts)
            navigator.evaluateJavaScript(
                script.bufferedReader().use { it.readText() }
            )
            isLoading.value = false
        }
    }

    state.webSettings.apply {
        isJavaScriptEnabled = true
        supportZoom = false

        androidWebSettings.apply {
            domStorageEnabled = true
            hideDefaultVideoPoster = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true

        }
    }

    if (isError && isLoading.value) {
        NetworkErrorDialog(context)
        return
    }

    if (isLoading.value)
        SplashLoading(state.loadingState)

    WebView(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        state = state,
        navigator = navigator,
        platformWebViewParams = fileChooserWebViewParams(),
        onCreated = { webView ->
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true)
            cookieManager.flush()

            webView.apply {

                addJavascriptInterface(ThemeChange(window), "ThemeBridge")
                addJavascriptInterface(DownloadBridge(context), "DownloadBridge")
                overScrollMode = View.OVER_SCROLL_NEVER
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false

                setDownloadListener { url, _, contentDisposition, mimeType, _ ->
                            Toast.makeText(context, "Processing blob download...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}
