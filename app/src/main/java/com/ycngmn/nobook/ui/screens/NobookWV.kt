package com.ycngmn.nobook.ui.screens

import android.content.Intent
import android.view.View
import android.webkit.CookieManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberSaveableWebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.ycngmn.nobook.R
import com.ycngmn.nobook.ui.components.BrowserBottomBar
import com.ycngmn.nobook.ui.components.NetworkErrorDialog
import com.ycngmn.nobook.ui.components.settings.SettingsDialog
import com.ycngmn.nobook.ui.viewmodel.MainViewModel
import com.ycngmn.nobook.ui.viewmodel.SettingsViewModel
import com.ycngmn.nobook.utils.DESKTOP_USER_AGENT
import com.ycngmn.nobook.utils.ExternalRequestInterceptor
import com.ycngmn.nobook.utils.fileChooserWebViewParams
import com.ycngmn.nobook.utils.jsBridge.BrowserUiBridge
import com.ycngmn.nobook.utils.jsBridge.ClipboardBridge
import com.ycngmn.nobook.utils.jsBridge.DownloadBridge
import com.ycngmn.nobook.utils.jsBridge.ThemeChange
import com.ycngmn.nobook.utils.rememberAutoDesktop
import com.ycngmn.nobook.utils.rememberImeHeight
import kotlinx.coroutines.delay

private const val FACEBOOK_HOME_URL =
    "https://www.facebook.com/"

@Composable
fun NobookWebView(
    url: String,
    settingsVM: SettingsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val resources = LocalResources.current

    val state = rememberSaveableWebViewState(url)

    val navigator = rememberWebViewNavigator(
        requestInterceptor =
            ExternalRequestInterceptor { externalUrl ->
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    externalUrl.toUri(),
                )

                runCatching {
                    context.startActivity(intent)
                }.onFailure {
                    Toast.makeText(
                        context,
                        resources.getString(
                            R.string.not_supported,
                        ),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            },
    )

    LaunchedEffect(navigator) {
        if (state.viewState == null) {
            navigator.loadUrl(url)
        }
    }

    var nativeWebView by remember {
        mutableStateOf<android.webkit.WebView?>(null)
    }

    var exitScroll by remember {
        mutableStateOf(false)
    }

    BackHandler {
        if (exitScroll) {
            activity?.finish()
        } else {
            navigator.evaluateJavaScript(
                "backHandlerNB();",
            ) { result ->
                val backHandled =
                    result.removeSurrounding("\"")

                when (backHandled) {
                    "false" -> {
                        if (navigator.canGoBack) {
                            navigator.navigateBack()
                        } else {
                            activity?.finish()
                        }
                    }

                    "exit" -> {
                        activity?.finish()
                    }

                    "scrolling" -> {
                        exitScroll = true
                    }
                }
            }
        }
    }

    LaunchedEffect(exitScroll) {
        if (exitScroll) {
            delay(800)
            exitScroll = false
        }
    }

    val isDesktop by
    settingsVM.desktopLayout.collectAsState()

    val isAutoRevert by
    settingsVM.isRevertDesktop.collectAsState()

    val isAutoDesktop = rememberAutoDesktop()

    LaunchedEffect(
        isAutoDesktop,
        isDesktop,
        isAutoRevert,
    ) {
        if (isAutoDesktop && !isDesktop) {
            settingsVM.setRevertDesktop(true)
            settingsVM.setDesktopLayout(true)
        } else if (!isAutoDesktop && isAutoRevert) {
            settingsVM.setRevertDesktop(false)
            settingsVM.setDesktopLayout(false)
        }
    }

    var isLoading by rememberSaveable {
        mutableStateOf(true)
    }

    /*
     * Controlled by scripts.js:
     * - hidden when scrolling down
     * - visible when scrolling up
     * - hidden inside media viewers
     */
    var isBottomBarVisible by remember {
        mutableStateOf(true)
    }

    val browserUiBridge: BrowserUiBridge = remember {
        BrowserUiBridge { visible: Boolean ->
            isBottomBarVisible = visible
        }
    }

    val isError =
        state.errorsForCurrentRequest
            .lastOrNull()
            ?.isFromMainFrame == true

    val viewModel: MainViewModel = viewModel {
        MainViewModel(
            resources = resources,
            settings = settingsVM,
        )
    }

    val themeColor by viewModel.themeColor
    val userScripts by viewModel.scripts
    val loadingState = state.loadingState

    var isImmersiveMode by rememberSaveable {
        mutableStateOf(
            settingsVM.immersiveMode.value,
        )
    }

    fun setWindow(immersive: Boolean) {
        val window = activity?.window ?: return

        val insetsController =
            WindowInsetsControllerCompat(
                window,
                window.decorView,
            )

        if (immersive) {
            insetsController.hide(
                WindowInsetsCompat.Type.systemBars(),
            )

            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat
                    .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            val isLight =
                ColorUtils.calculateLuminance(
                    themeColor.toArgb(),
                ) > 0.5

            insetsController.show(
                WindowInsetsCompat.Type.systemBars(),
            )

            insetsController
                .isAppearanceLightStatusBars = isLight

            insetsController
                .isAppearanceLightNavigationBars = isLight
        }

        isImmersiveMode = immersive
    }

    LaunchedEffect(
        isImmersiveMode,
        themeColor,
    ) {
        setWindow(isImmersiveMode)
    }

    LaunchedEffect(
        isDesktop,
        nativeWebView,
    ) {
        nativeWebView
            ?.settings
            ?.userAgentString =
            if (isDesktop) {
                DESKTOP_USER_AGENT
            } else {
                ""
            }
    }

    /*
     * Inject the enabled scripts after each completed page load.
     */
    LaunchedEffect(
        loadingState,
        userScripts,
    ) {
        if (loadingState is LoadingState.Finished) {
            userScripts?.let { scripts ->
                navigator.evaluateJavaScript(
                    scripts,
                ) {
                    /*
                     * Ask the controller to immediately check the
                     * current page after all scripts are installed.
                     */
                    navigator.evaluateJavaScript(
                        """
                        if (
                            window.NobookBottomBarController &&
                            window.NobookBottomBarController.refresh
                        ) {
                            window.NobookBottomBarController.refresh();
                        }
                        """.trimIndent(),
                    )

                    isLoading = false
                }
            }
        }
    }

    if (isError && isLoading) {
        NetworkErrorDialog {
            activity?.finish()
        }

        return
    }

    var settingsToggle by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(settingsToggle) {
        if (settingsToggle) {
            setWindow(false)
        }
    }

    if (settingsToggle) {
        SettingsDialog(
            themeColor = themeColor,
            onDismiss = {
                settingsToggle = false

                setWindow(
                    settingsVM.immersiveMode.value,
                )
            },
            onReload = {
                isLoading = true
                isBottomBarVisible = false

                viewModel.setThemeColor(
                    Color.Transparent,
                )

                setWindow(
                    settingsVM.immersiveMode.value,
                )

                viewModel.refresh(
                    resources = resources,
                    settings = settingsVM,
                )

                navigator.reload()
            },
        )
    }

    val imeHeight = rememberImeHeight()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (!isImmersiveMode) {
                    Modifier.statusBarsPadding()
                } else {
                    Modifier
                },
            ),
    ) {
        WebView(
            modifier = Modifier
                .fillMaxSize()
                .background(themeColor)
                .padding(bottom = imeHeight),
            state = state,
            navigator = navigator,
            platformWebViewParams =
                fileChooserWebViewParams(),
            captureBackPresses = false,
            onCreated = { webView ->
                nativeWebView = webView

                val cookieManager =
                    CookieManager.getInstance()

                cookieManager.setAcceptCookie(true)

                cookieManager.setAcceptThirdPartyCookies(
                    webView,
                    true,
                )

                cookieManager.flush()

                state.webSettings.apply {
                    isJavaScriptEnabled = true

                    androidWebSettings.apply {
                        domStorageEnabled = true
                        hideDefaultVideoPoster = true
                        mediaPlaybackRequiresUserGesture =
                            false
                    }
                }

                webView.apply {
                    /*
                     * Controls native bottom-bar visibility.
                     */
                    addJavascriptInterface(
                        browserUiBridge,
                        "BrowserUiBridge",
                    )

                    addJavascriptInterface(
                        ThemeChange { colorValue ->
                            viewModel.setThemeColor(
                                Color(colorValue),
                            )
                        },
                        "ThemeBridge",
                    )

                    /*
                     * Keeps the original media-download feature.
                     * No native download icon is displayed.
                     */
                    addJavascriptInterface(
                        DownloadBridge(context),
                        "DownloadBridge",
                    )

                    addJavascriptInterface(
                        ClipboardBridge(context),
                        "ClipboardBridge",
                    )

                    setLayerType(
                        View.LAYER_TYPE_HARDWARE,
                        null,
                    )

                    overScrollMode =
                        View.OVER_SCROLL_NEVER

                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false

                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                }
            },
        )

        if (isLoading) {
            SplashLoading(
                progress =
                    if (
                        loadingState is
                                LoadingState.Loading
                    ) {
                        loadingState.progress
                    } else {
                        0.8f
                    },
            )
        }

        if (
            !isImmersiveMode &&
            !isLoading &&
            !settingsToggle &&
            isBottomBarVisible
        ) {
            BrowserBottomBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 14.dp),
                canGoBack = navigator.canGoBack,
                onBack = {
                    if (navigator.canGoBack) {
                        navigator.navigateBack()
                    }
                },
                onHome = {
                    isLoading = true
                    isBottomBarVisible = false

                    navigator.loadUrl(
                        FACEBOOK_HOME_URL,
                    )
                },
                onRefresh = {
                    isLoading = true
                    isBottomBarVisible = false
                    navigator.reload()
                },
                onSettings = {
                    settingsToggle = true
                },
            )
        }
    }
}