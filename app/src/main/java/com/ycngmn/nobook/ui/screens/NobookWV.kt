package com.ycngmn.nobook.ui.screens

import android.content.Intent
import android.view.View
import android.webkit.CookieManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.ycngmn.nobook.NobookViewModel
import com.ycngmn.nobook.ui.components.NetworkErrorDialog
import com.ycngmn.nobook.ui.components.settings.SettingsDialog
import com.ycngmn.nobook.utils.ExternalRequestInterceptor
import com.ycngmn.nobook.utils.fileChooserWebViewParams
import com.ycngmn.nobook.utils.getDesktopUserAgent
import com.ycngmn.nobook.utils.isAutoDesktop
import com.ycngmn.nobook.utils.jsBridge.DownloadBridge
import com.ycngmn.nobook.utils.jsBridge.NobookSettings
import com.ycngmn.nobook.utils.jsBridge.ThemeChange
import com.ycngmn.nobook.utils.rememberImeHeight
import kotlinx.coroutines.delay

@Composable
fun NobookWebView(
    url: String,
    viewModel: NobookViewModel
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val resources = LocalResources.current

    val state = rememberWebViewState(url)
    val navigator = rememberWebViewNavigator(
        requestInterceptor = ExternalRequestInterceptor {
            val intent = Intent.parseUri(it, Intent.URI_INTENT_SCHEME)
            context.startActivity(intent)
        }
    )

    // allow exiting while scrolling to top.
    var exit by remember { mutableStateOf(false) }

    BackHandler {
        if (exit) activity?.finish()
        else navigator.evaluateJavaScript("backHandlerNB();") {
            val backHandled = it.removeSurrounding("\"")
            when (backHandled) {
                "false" -> {
                    if (navigator.canGoBack) navigator.navigateBack()
                    else activity?.finish()
                }
                "exit" -> activity?.finish()
                else -> exit = true
            }
        }
    }

    LaunchedEffect(exit) {
        if (exit) {
            delay(800)
            exit = false
        }
    }

    val isDesktop = viewModel.desktopLayout.collectAsState()
    val isAutoRevert = viewModel.isRevertDesktop.value
    val isAutoDesktop = isAutoDesktop()

    LaunchedEffect(Unit) {
        if (isAutoDesktop && !isDesktop.value) {
            viewModel.setRevertDesktop(true)
            viewModel.setDesktopLayout(true)
        }
        else if (!isAutoDesktop && isAutoRevert) {
            viewModel.setRevertDesktop(false)
            viewModel.setDesktopLayout(false)
        }
    }

    var isLoading by rememberSaveable { mutableStateOf(true) }
    val isError = state.errorsForCurrentRequest.lastOrNull()?.isFromMainFrame == true

    val themeColor = viewModel.themeColor.collectAsState().value
    // Manual handling to fix visual & padding bug on settings dialog.
    var isImmersiveMode by rememberSaveable { mutableStateOf(viewModel.immersiveMode.value) }

    fun setWindow(immersive: Boolean) {
        val window = activity?.window ?: return
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)

        if (immersive) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            val isLight = ColorUtils.calculateLuminance(themeColor.toArgb()) > 0.5
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.isAppearanceLightStatusBars = isLight
            windowInsetsController.isAppearanceLightNavigationBars = isLight
        }
        isImmersiveMode = immersive
    }
    LaunchedEffect(isImmersiveMode, themeColor.value) {
        setWindow(isImmersiveMode)
    }

    val userScripts = viewModel.scripts.collectAsState().value
    val loadingState = state.loadingState
    LaunchedEffect(userScripts) {
        if (userScripts == null)
            viewModel.loadScripts(resources)
    }

    LaunchedEffect(loadingState, userScripts) {
        if (loadingState is LoadingState.Finished && userScripts != null) {
            navigator.evaluateJavaScript(userScripts) { isLoading = false }
        } else { isLoading = userScripts == null || loadingState is LoadingState.Loading }
    }

    if (isError && isLoading) {
        NetworkErrorDialog { activity?.finish() }
        return
    }

    var settingsToggle by rememberSaveable { mutableStateOf(false) }
    if (settingsToggle) {
        setWindow(false)
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = {
                setWindow(viewModel.immersiveMode.value)
                settingsToggle = false
            },
            onReload = {
                setWindow(viewModel.immersiveMode.value)
                settingsToggle = false
                viewModel.setScripts(null)
                navigator.reload()
            }
        )
    }

    if (isLoading) {
        SplashLoading(
            if (loadingState is LoadingState.Loading)
                loadingState.progress
            else 0.8F
        )
    }

    val userAgent = if (isDesktop.value) getDesktopUserAgent() else ""
    LaunchedEffect(userAgent) { state.nativeWebView.settings.userAgentString = userAgent }

    WebView(
        modifier = Modifier.fillMaxSize()
            .background(themeColor)
            .padding(bottom = rememberImeHeight())
            .then(if (!isImmersiveMode) Modifier.systemBarsPadding() else Modifier),
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
                }
            }

            webView.apply {
                addJavascriptInterface(
                    NobookSettings { settingsToggle = true },
                    "SettingsBridge"
                )
                addJavascriptInterface(
                    ThemeChange { viewModel.setThemeColor(Color(it)) },
                    "ThemeBridge"
                )
                addJavascriptInterface(DownloadBridge(context), "DownloadBridge")

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