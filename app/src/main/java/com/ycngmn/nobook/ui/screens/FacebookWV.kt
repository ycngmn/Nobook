package com.ycngmn.nobook.ui.screens

import android.app.Activity
import android.view.View
import android.webkit.CookieManager
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
import com.ycngmn.nobook.utils.ExternalRequestInterceptor
import com.ycngmn.nobook.utils.ThemeChangeInterface
import com.ycngmn.nobook.utils.fileChooserWebViewParams
import com.ycngmn.nobook.utils.sponsoredAdBlockerScript
import com.ycngmn.nobook.utils.styling.HIDE_OPEN_WITH_APP_BANNER_SCRIPT
import com.ycngmn.nobook.utils.styling.colorExtractionScript
import com.ycngmn.nobook.utils.styling.enhanceLoadingOverlayScript
import com.ycngmn.nobook.utils.styling.holdEffectScript
import com.ycngmn.nobook.utils.styling.stickyTopNavbarScript
import com.ycngmn.nobook.utils.zoomDisableScript

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
            navigator.evaluateJavaScript(
                HIDE_OPEN_WITH_APP_BANNER_SCRIPT +
                zoomDisableScript +
                sponsoredAdBlockerScript +
                holdEffectScript +
                enhanceLoadingOverlayScript +
                stickyTopNavbarScript +
                colorExtractionScript
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
        }
    }

    if (isError && isLoading.value) {
        NetworkErrorDialog(context)
        return
    }

    if (isLoading.value)
        SplashLoading(state.loadingState)

    WebView(
        modifier = Modifier.fillMaxSize().imePadding(),
        state = state,
        navigator = navigator,
        platformWebViewParams = fileChooserWebViewParams(),
        onCreated = { webView ->
            // Save cookies to retain logins and stuff.
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true)
            CookieManager.getInstance().flush()

            webView.apply {
                // To debug, connect the device to the computer and go to chrome://inspect
                //isDebugInspectorInfoEnabled = true
                addJavascriptInterface(ThemeChangeInterface(window), "ThemeBridge")
                // Hide scrollbars
                overScrollMode = View.OVER_SCROLL_NEVER
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
            }
        }
    )
}



