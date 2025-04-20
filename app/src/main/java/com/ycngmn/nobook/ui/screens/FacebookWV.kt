package com.ycngmn.nobook.ui.screens

import android.view.View
import android.webkit.CookieManager
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
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

    val isLoading = remember { mutableStateOf(true) }
    val isError = state.errorsForCurrentRequest.isNotEmpty()
    val hasShownErrorToast = remember { mutableStateOf(false) }

    LaunchedEffect(state.loadingState) {
        if (state.loadingState is LoadingState.Finished && !isError) {
            navigator.evaluateJavaScript(
                HIDE_OPEN_WITH_APP_BANNER_SCRIPT +
                zoomDisableScript +
                sponsoredAdBlockerScript +
                holdEffectScript +
                enhanceLoadingOverlayScript +
                removeBottomPaddingScript
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

    if (isError) {
        if (isLoading.value) {
            NetworkErrorDialog(context)
            return
        } else if (!hasShownErrorToast.value) {
            Toast.makeText(context, "Connection error", Toast.LENGTH_LONG).show()
            hasShownErrorToast.value = true
        }

    }

    if (isLoading.value)
        SplashLoading(state.loadingState)

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
                overScrollMode = View.OVER_SCROLL_NEVER
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
            }
        }
    )

}