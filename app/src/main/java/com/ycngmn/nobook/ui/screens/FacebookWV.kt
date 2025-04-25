package com.ycngmn.nobook.ui.screens

import android.app.Activity
import android.view.View
import android.webkit.CookieManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsControllerCompat
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
    val window =  (context as Activity).window

    val state = rememberWebViewState("https://m.facebook.com")
    val navigator = rememberWebViewNavigator(
        requestInterceptor = ExternalRequestInterceptor(context = context) {
            onOpenMessenger()
        }
    )

    val isLoading = remember { mutableStateOf(true) }
    val isError = state.errorsForCurrentRequest.lastOrNull()?.isFromMainFrame == true
    val themeColor = remember { mutableStateOf(Color.Transparent) }

    LaunchedEffect(themeColor.value) {
        val isLight = ColorUtils.calculateLuminance(themeColor.value.toArgb()) > 0.5
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColor.value)
    ) {
        WebView(
            modifier = Modifier
                .imePadding().systemBarsPadding(),
            state = state,
            navigator = navigator,
            platformWebViewParams = fileChooserWebViewParams(),
            onCreated = { webView ->
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(webView, true)
                cookieManager.flush()

                webView.apply {
                    addJavascriptInterface(ThemeChange(themeColor), "ThemeBridge")
                    addJavascriptInterface(DownloadBridge(context), "DownloadBridge")
                    overScrollMode = View.OVER_SCROLL_NEVER
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false

                    setDownloadListener { url, _, contentDisposition, mimeType, _ ->
                        Toast.makeText(context, "Processing blob download...", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        )
    }
}
