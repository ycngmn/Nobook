package com.ycngmn.nobook.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
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
import com.ycngmn.nobook.ui.components.sheet.NobookSheet
import com.ycngmn.nobook.utils.ExternalRequestInterceptor
import com.ycngmn.nobook.utils.fileChooserWebViewParams
import com.ycngmn.nobook.utils.jsBridge.DownloadBridge
import com.ycngmn.nobook.utils.jsBridge.NavigateFB
import com.ycngmn.nobook.utils.jsBridge.NobookSettings
import com.ycngmn.nobook.utils.jsBridge.ThemeChange


@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun BaseWebView(
    url: String,
    userAgent: String? = null,
    onInterceptAction: (() -> Unit) = {},
    onPostLoad: (WebViewNavigator, Context) -> Unit = { _, _ -> },
    onRestart: () -> Unit = {},
) {
    val context = LocalContext.current
    val window = (context as Activity).window

    // Lock orientation to portrait as fb mobile isn't optimized for landscape mode.
    context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    val state = rememberWebViewState(url, additionalHttpHeaders = mapOf("X-Requested-With" to ""))
    val navigator = rememberWebViewNavigator(requestInterceptor =
        ExternalRequestInterceptor(context = context, onInterceptAction))

    if (state.lastLoadedUrl?.contains("facebook.com/messages/blocked") == true) {
        Toast.makeText(context, "Opening messages...", Toast.LENGTH_SHORT).show()
        onInterceptAction()
    }

    // To navigate away from messenger
    val navTrigger = remember { mutableStateOf(false) }
    if (navTrigger.value) onInterceptAction()

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

    if (isLoading.value) SplashLoading(state.loadingState)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorState.value)
    ) {

        if (settingsToggle.value) {
            NobookSheet(settingsToggle, context, onRestart)
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

                // Configure WebView
                state.webSettings.apply {
                    customUserAgentString = userAgent
                    isJavaScriptEnabled = true

                    androidWebSettings.apply {
                        //isDebugInspectorInfoEnabled = true
                        useWideViewPort = true
                        domStorageEnabled = true
                        hideDefaultVideoPoster = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = true
                    }
                }

                webView.apply {
                    addJavascriptInterface(NobookSettings(settingsToggle), "SettingsBridge")
                    addJavascriptInterface(ThemeChange(colorState), "ThemeBridge")
                    addJavascriptInterface(DownloadBridge(context), "DownloadBridge")
                    addJavascriptInterface(NavigateFB(navTrigger), "NavigateBridge")

                    setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    // Hide scrollbars
                    overScrollMode = View.OVER_SCROLL_NEVER
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false

                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false

                    settings.loadWithOverviewMode = true
                }
            }
        )
    }
}