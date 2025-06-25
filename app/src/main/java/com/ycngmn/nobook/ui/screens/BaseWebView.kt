package com.ycngmn.nobook.ui.screens

import android.view.View
import android.webkit.CookieManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.ycngmn.nobook.ui.NobookViewModel
import com.ycngmn.nobook.ui.components.NetworkErrorDialog
import com.ycngmn.nobook.ui.components.sheet.NobookSheet
import com.ycngmn.nobook.utils.ExternalRequestInterceptor
import com.ycngmn.nobook.utils.fileChooserWebViewParams
import com.ycngmn.nobook.utils.isAutoDesktop
import com.ycngmn.nobook.utils.jsBridge.DownloadBridge
import com.ycngmn.nobook.utils.jsBridge.NavigateFB
import com.ycngmn.nobook.utils.jsBridge.NobookSettings
import com.ycngmn.nobook.utils.jsBridge.ThemeChange
import kotlinx.coroutines.delay
import rememberImeHeight


@Composable
fun BaseWebView(
    url: String,
    userAgent: String? = null,
    onInterceptAction: (() -> Unit) = {},
    onPostLoad: () -> Unit = {},
    onRestart: () -> Unit = {},
    viewModel: NobookViewModel,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val state =
        rememberWebViewState(url, additionalHttpHeaders = mapOf("X-Requested-With" to ""))
    val navigator = rememberWebViewNavigator(requestInterceptor =
        ExternalRequestInterceptor(context = context, onInterceptAction))



    // allow exiting while scrolling to top.
    val exit = remember { mutableStateOf(false) }
    LaunchedEffect(exit.value) {
        if (exit.value) {
            delay(800)
            exit.value = false
        }
    }

    BackHandler {
        if (exit.value) activity?.finish()
        else navigator.evaluateJavaScript("backHandlerNB();") {
            val backHandled = it.removeSurrounding("\"")
            if (backHandled == "false") {
                if (navigator.canGoBack) navigator.navigateBack()
                else if (state.lastLoadedUrl?.contains(".facebook.com/messages/") == true)
                    onInterceptAction()
                else activity?.finish()
            }
            else if (backHandled == "exit") activity?.finish()
            else exit.value = true
        }
    }


    // Navigate to Nobook on fb logo pressed from messenger.
    val navTrigger = remember { mutableStateOf(false) }
    if (navTrigger.value) onInterceptAction()

    val isLoading = remember { mutableStateOf(true) }
    val isError = state.errorsForCurrentRequest.lastOrNull()?.isFromMainFrame == true

    val settingsToggle = remember { mutableStateOf(false) }
    val themeColor = viewModel.themeColor
    val isImmersiveMode = viewModel.immersiveMode.collectAsState()

    LaunchedEffect(isImmersiveMode.value, themeColor.value) {
        val window = activity?.window
        if (window != null) {
            val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
            if (isImmersiveMode.value) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                windowInsetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            else {
                val isLight = ColorUtils.calculateLuminance(themeColor.value.toArgb()) > 0.5
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                windowInsetsController.isAppearanceLightStatusBars = isLight
                windowInsetsController.isAppearanceLightNavigationBars = isLight
            }
        }
    }

    val userScripts = viewModel.scripts
    if (userScripts.value.isEmpty()) {
        LaunchedEffect(Unit) { onPostLoad() } }

    LaunchedEffect(state.loadingState, userScripts.value) {
        if (state.loadingState is LoadingState.Finished && userScripts.value.isNotEmpty()){
            navigator.evaluateJavaScript(userScripts.value) {
                if (isLoading.value) isLoading.value = false
            }
        }
        else if (state.loadingState is LoadingState.Loading) isLoading.value = true
    }

    if (isError && isLoading.value) {
        NetworkErrorDialog(context)
        return
    }

    if (settingsToggle.value) NobookSheet(viewModel, settingsToggle, onRestart)
    // A possible overkill to fix https://github.com/ycngmn/Nobook/issues/5
    if (state.lastLoadedUrl?.contains(".com/messages/blocked") == true) onInterceptAction()

    if (isLoading.value) SplashLoading(state.loadingState)

    val wvModifier = Modifier
        .fillMaxSize()
        .background(themeColor.value)

    val barsInsets = WindowInsets.systemBars.asPaddingValues()
    val imeHeight = rememberImeHeight()

    // we limit the recomposition to specific cases with the condition.
    key(if (isAutoDesktop() || viewModel.isRevertDesktop.value) userAgent else null) {
        WebView(
            modifier =
                if (isImmersiveMode.value) wvModifier.padding(bottom = imeHeight)
                else wvModifier.padding(
                    top = barsInsets.calculateTopPadding(),
                    bottom = maxOf(barsInsets.calculateBottomPadding(), imeHeight)
                ),
            state = state,
            navigator = navigator,
            platformWebViewParams = fileChooserWebViewParams(),
            captureBackPresses = false,
            onCreated = { webView ->

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(webView, true)
                cookieManager.flush()

                state.webSettings.apply {
                    customUserAgentString = userAgent
                    isJavaScriptEnabled = true

                    androidWebSettings.apply {
                        //isDebugInspectorInfoEnabled = true
                        domStorageEnabled = true
                        hideDefaultVideoPoster = true
                        mediaPlaybackRequiresUserGesture = false
                        textZoom = 96
                    }
                }

                webView.apply {
                    addJavascriptInterface(NobookSettings(settingsToggle), "SettingsBridge")
                    addJavascriptInterface(ThemeChange(themeColor), "ThemeBridge")
                    addJavascriptInterface(DownloadBridge(context), "DownloadBridge")
                    addJavascriptInterface(NavigateFB(navTrigger), "NavigateBridge")

                    setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    // Hide scrollbars
                    overScrollMode = View.OVER_SCROLL_NEVER
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false

                    settings.setSupportZoom(true)
                    // pinch to zoom doesn't work on settings refresh otherwise
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                }
            }
        )
    }
}