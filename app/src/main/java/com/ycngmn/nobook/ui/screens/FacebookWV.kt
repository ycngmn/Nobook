package com.ycngmn.nobook.ui.screens

import android.view.View
import android.webkit.CookieManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.ycngmn.nobook.ui.components.NetworkErrorDialog
import com.ycngmn.nobook.utils.ExternalRequestInterceptor
import com.ycngmn.nobook.utils.getPlatformWebViewParams
import com.ycngmn.nobook.utils.sponsoredAdBlockerScript
import com.ycngmn.nobook.utils.styling.HIDE_OPEN_WITH_APP_BANNER_SCRIPT
import com.ycngmn.nobook.utils.styling.enhanceLoadingOverlayScript
import com.ycngmn.nobook.utils.styling.holdEffectScript
import com.ycngmn.nobook.utils.styling.removeBottomPaddingScript
import com.ycngmn.nobook.utils.styling.stickyTopNavbarScript
import com.ycngmn.nobook.utils.zoomDisableScript


@Composable
fun FacebookWebView() {

    val context = LocalContext.current

    val state = rememberWebViewState("https://m.facebook.com")
    val navigator = rememberWebViewNavigator(
        requestInterceptor = ExternalRequestInterceptor(context = context)
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
                removeBottomPaddingScript +
                stickyTopNavbarScript
            )
            isLoading.value = false
        }
    }

    state.webSettings.apply {
        isJavaScriptEnabled = true
        supportZoom = false

        androidWebSettings.apply {
            hideDefaultVideoPoster = true
            allowFileAccess = true
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
        modifier = Modifier.fillMaxSize(),
        state = state,
        navigator = navigator,
        platformWebViewParams = getPlatformWebViewParams(),
        onCreated = { webView ->
            // Save cookies to retain logins and stuff.
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true)

            webView.apply {
                isDebugInspectorInfoEnabled = true

                // Hide scrollbars
                overScrollMode = View.OVER_SCROLL_NEVER
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
            }
        }
    )

}