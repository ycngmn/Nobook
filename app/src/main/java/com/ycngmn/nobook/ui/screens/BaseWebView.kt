package com.ycngmn.nobook.ui.screens

import android.app.Activity
import android.content.Context
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
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.ycngmn.nobook.ui.components.NetworkErrorDialog
import com.ycngmn.nobook.ui.components.NobookSheet
import com.ycngmn.nobook.utils.ExternalRequestInterceptor
import com.ycngmn.nobook.utils.fileChooserWebViewParams
import com.ycngmn.nobook.utils.jsBridge.DownloadBridge
import com.ycngmn.nobook.utils.jsBridge.NobookSettings
import com.ycngmn.nobook.utils.jsBridge.ThemeChange


@Composable
fun BaseWebView(
    url: String,
    userAgent: String? = null,
    onInterceptAction: (() -> Unit) = {},
    onPostLoad: (WebViewNavigator, Context) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val window = (context as Activity).window

    val state = rememberWebViewState(url)
    val navigator = rememberWebViewNavigator(requestInterceptor =
        ExternalRequestInterceptor(context = context, onInterceptAction))


    val isLoading = remember { mutableStateOf(true) }
    val isError = state.errorsForCurrentRequest.lastOrNull()?.isFromMainFrame == true
    val colorState = remember { mutableStateOf(Color.Transparent) }
    val settingsToggle = remember { mutableStateOf(false) }


    LaunchedEffect(colorState.value) {
        // Set status bar items color based on the brightness of its background
        val isLight = ColorUtils.calculateLuminance(colorState.value.toArgb()) > 0.5
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
    }

    // Handle loading state changes
    LaunchedEffect(state.loadingState) {
        if (state.loadingState is LoadingState.Finished && !isError) {
            onPostLoad(navigator, context)
            isLoading.value = false
        }
    }

    // Show error dialog on network fail
    if (isError && isLoading.value) {
        NetworkErrorDialog(context)
        return
    }

    if (isLoading.value) {
        SplashLoading(state.loadingState)
    }

    // Configure WebView

    state.webSettings.apply {
        customUserAgentString = userAgent
        isJavaScriptEnabled = true
        supportZoom = false

        androidWebSettings.apply {
            domStorageEnabled = true
            hideDefaultVideoPoster = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorState.value)
    ) {

        if (settingsToggle.value) {
            NobookSheet(settingsToggle)
        }

        WebView(
            modifier = Modifier
                .imePadding()
                .systemBarsPadding(),
            state = state,
            navigator = navigator,
            platformWebViewParams = fileChooserWebViewParams(),
            onCreated = { webView ->
                // Set up cookies
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(webView, true)
                cookieManager.flush()

                webView.apply {
                    addJavascriptInterface(NobookSettings(settingsToggle), "SettingsBridge")
                    addJavascriptInterface(ThemeChange(colorState), "ThemeBridge")
                    addJavascriptInterface(DownloadBridge(context), "DownloadBridge")

                    // Hide scrollbars
                    overScrollMode = View.OVER_SCROLL_NEVER
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false

                    // Support downloads
                    setDownloadListener { downloadUrl, _, contentDisposition, mimeType, _ ->
                        Toast.makeText(context, "Processing blob download...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}