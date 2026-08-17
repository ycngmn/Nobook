package com.ycngmn.nobook.ui.screens

import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.CookieManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberSaveableWebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.ycngmn.nobook.R
import com.ycngmn.nobook.ui.components.NetworkErrorDialog
import com.ycngmn.nobook.ui.components.settings.SettingsDialog
import com.ycngmn.nobook.ui.viewmodel.MainViewModel
import com.ycngmn.nobook.ui.viewmodel.SettingsViewModel
import com.ycngmn.nobook.utils.DESKTOP_USER_AGENT
import com.ycngmn.nobook.utils.ExternalRequestInterceptor
import com.ycngmn.nobook.utils.fileChooserWebViewParams
import com.ycngmn.nobook.utils.jsBridge.ClipboardBridge
import com.ycngmn.nobook.utils.jsBridge.DownloadBridge
import com.ycngmn.nobook.utils.jsBridge.DownloadFolderBridge
import com.ycngmn.nobook.utils.jsBridge.DownloadFolderPicker
import com.ycngmn.nobook.utils.jsBridge.NobookSettings
import com.ycngmn.nobook.utils.jsBridge.ThemeChange
import com.ycngmn.nobook.utils.rememberAutoDesktop
import com.ycngmn.nobook.utils.rememberImeHeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

private const val ANTI_RELOAD_SCRIPT = """
(function () {
  try {
    if (window.__nobookAntiReloadActive) return;
    window.__nobookAntiReloadActive = true;
    var defineAlways = function (obj, prop, value) {
      try { Object.defineProperty(obj, prop, { configurable: true, get: function () { return value; } }); } catch (e) {}
    };
    defineAlways(document, "visibilityState", "visible");
    defineAlways(document, "hidden", false);
    defineAlways(document, "webkitVisibilityState", "visible");
    defineAlways(document, "webkitHidden", false);
    var blocked = ["visibilitychange", "webkitvisibilitychange", "blur", "pagehide", "freeze"];
    var origAdd = EventTarget.prototype.addEventListener;
    var origDispatch = EventTarget.prototype.dispatchEvent;
    EventTarget.prototype.addEventListener = function (type, listener, options) {
      if (blocked.indexOf(type) !== -1) return;
      return origAdd.call(this, type, listener, options);
    };
    EventTarget.prototype.dispatchEvent = function (evt) {
      if (evt && blocked.indexOf(evt.type) !== -1) return true;
      return origDispatch.call(this, evt);
    };
    window.onblur = null;
    window.onpagehide = null;
    document.onvisibilitychange = null;
    Object.defineProperty(document, "hasFocus", { configurable: true, value: function () { return true; } });
    console.info("[Nobook] Anti-Reload guard active");
  } catch (err) {
    console.error("[Nobook] Anti-Reload injection failed:", err);
  }
})();
"""

private const val PERFORMANCE_OPTIMIZATION_SCRIPT = """
(function () {
  try {
    if (window.__nobookPerformanceOptActive) return;
    window.__nobookPerformanceOptActive = true;
    var CULL_CSS = `div[role="article"], div[data-pagelet^="FeedUnit"] { content-visibility: auto; contain-intrinsic-size: 600px 400px; }`;
    var style = document.createElement('style');
    style.setAttribute('data-nobook-perf', '1');
    style.textContent = CULL_CSS;
    document.head.appendChild(style);
    var observedVideos = new WeakSet();
    var handleIntersections = function (entries) {
      entries.forEach(function (entry) {
        var video = entry.target;
        if (entry.isIntersecting && entry.intersectionRatio > 0.25) {
          if (video.hasAttribute('data-nobook-paused')) {
            video.removeAttribute('data-nobook-paused');
            video.preload = 'auto';
            if (video.dataset.nobookWasPlaying === '1') {
              var p = video.play();
              if (p && typeof p.catch === 'function') p.catch(function () {});
            }
          }
        } else {
          if (!video.paused) { video.dataset.nobookWasPlaying = '1'; video.pause(); } else { video.dataset.nobookWasPlaying = '0'; }
          video.setAttribute('data-nobook-paused', '1');
          video.preload = 'none';
        }
      });
    };
    var io = new IntersectionObserver(handleIntersections, { root: null, rootMargin: '200px 0px', threshold: [0, 0.25, 0.5] });
    var observeVideos = function () {
      document.querySelectorAll('video').forEach(function (v) {
        if (observedVideos.has(v)) return;
        observedVideos.add(v);
        io.observe(v);
      });
    };
    observeVideos();
    var mo = new MutationObserver(function () { observeVideos(); });
    mo.observe(document.body, { childList: true, subtree: true });
    console.info('[Nobook] Performance optimization (DOM culling + video IntersectionObserver) active');
  } catch (err) {
    console.error('[Nobook] Performance optimization injection failed:', err);
  }
})();
"""

@Composable
fun NobookWebView(
    url: String,
    settingsVM: SettingsViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = rememberSaveableWebViewState(url)
    val navigator = rememberWebViewNavigator()
    val loadingState = state.loadingState

    LaunchedEffect(loadingState) {
        if (loadingState is LoadingState.Finished) {
            navigator.evaluateJavaScript(ANTI_RELOAD_SCRIPT) {}
            navigator.evaluateJavaScript(PERFORMANCE_OPTIMIZATION_SCRIPT) {}
        }
    }

    DisposableEffect(lifecycleOwner, state) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    runCatching {
                        state.nativeWebView.onPause()
                        state.nativeWebView.pauseTimers()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    runCatching {
                        state.nativeWebView.onResume()
                        state.nativeWebView.resumeTimers()
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    WebView(
        modifier = Modifier.fillMaxSize(),
        state = state,
        navigator = navigator
    )
}
