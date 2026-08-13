package com.ycngmn.nobook.ui.screens

import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.CookieManager
import android.widget.Toast
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
import com.ycngmn.nobook.utils.jsBridge.NobookSettings
import com.ycngmn.nobook.utils.jsBridge.ThemeChange
import com.ycngmn.nobook.utils.rememberAutoDesktop
import com.ycngmn.nobook.utils.rememberImeHeight
import kotlinx.coroutines.delay

private const val ANTI_RELOAD_SCRIPT = """
(function () {
  try {
    if (window.__nobookAntiReloadActive) return;
    window.__nobookAntiReloadActive = true;

    var defineAlways = function (obj, prop, value) {
      try {
        Object.defineProperty(obj, prop, { configurable: true, get: function () { return value; } });
      } catch (e) {}
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

private val AFFILIATE_PARAM_PREFIXES = listOf("aff_", "utm_", "af_")
private val AFFILIATE_PARAM_EXACT = setOf("sub_id", "smtt", "is_from_signup", "fbclid", "ttclid", "gclid", "msclkid")

private fun sanitizeTrackingParams(url: String): String {
    return runCatching {
        val uri = Uri.parse(url)
        val builder = uri.buildUpon().clearQuery()
        for (paramName in uri.queryParameterNames) {
            val lower = paramName.lowercase()
            val isTrackingParam = AFFILIATE_PARAM_PREFIXES.any { lower.startsWith(it) } ||
                AFFILIATE_PARAM_EXACT.contains(lower)
            if (!isTrackingParam) {
                builder.appendQueryParameter(paramName, uri.getQueryParameter(paramName))
            }
        }
        builder.build().toString()
    }.getOrDefault(url)
}

private val DEFAULT_SITE_BLOCKLIST = setOf<String>(
    // Them domain (khong can http/https) vao day de mo rong blocklist tuy chinh.
)

private fun isBlockedSite(url: String): Boolean {
    if (DEFAULT_SITE_BLOCKLIST.isEmpty()) return false
    return DEFAULT_SITE_BLOCKLIST.any { blocked -> url.contains(blocked, ignoreCase = true) }
}

private const val STORY_REEL_DOWNLOADER_SCRIPT = """
/*
 * Script to add download buttons for stories, stories highlights and reels on Facebook
 * Original Author: @YeiversonYurgaky
 */
(function() {
  const CONFIG = {
    buttonZIndex: 999999,
    debug: false
  };

  let isProcessing = false;
  let currentContentContainer = null;
  let lastDownloadedUrl = null;
  const DOWNLOAD_BTN_ID = "nobook-global-downloader";

  const SELECTORS = {
    mediaElements: [
      'div[role="dialog"] video:not([hidden])',
      'div[role="dialog"] img[src*="fbcdn"]:not([width="16"]):not([hidden])',
      'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"] video',
      'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"] img[src*="fbcdn"]',
      'div[data-pagelet="Story"] video',
      'div[aria-label*="reel"] video',
      'div[data-pagelet="ProfilePhoto"] img[src*="fbcdn"]'
    ],
    containers: [
      'div[role="dialog"]',
      'div[data-pagelet="Story"]',
      'div[aria-label*="story"]',
      '.story-viewer',
      '.story_viewer',
      'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"]',
      'div[data-pagelet="ProfilePhoto"]',
      'div[aria-label*="photo"]',
      'div[data-pagelet*="ProfileAppSection"]'
    ],
    storyIndicators: [
      'div[data-sigil="story-viewer"]',
      'div[data-sigil="story-popup-header"]',
      'div[data-sigil="story-tray-item"]',
      ".story_body_container",
      ".story_viewer",
      ".story-container",
      'div[aria-label*="highlight"]',
      'div[aria-label*="Highlight"]',
      'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"]',
      'div[data-pagelet="ProfilePhoto"]'
    ]
  };

  const debugLog = (...args) => CONFIG.debug && console.log("[ContentDownloader]", ...args);

  const isElementVisible = (element) => {
    const rect = element.getBoundingClientRect();
    return (
      rect.top >= 0 &&
      rect.left >= 0 &&
      rect.bottom <= (window.innerHeight || document.documentElement.clientHeight)
    );
  };

  const findContentContainer = (element) => {
    if (!element) return null;
    for (const selector of SELECTORS.containers) {
      const container = element.closest(selector);
      if (container) return container;
    }
    return element.parentElement;
  };

  const getCurrentMediaElement = () => {
    for (const selector of SELECTORS.mediaElements) {
      const elements = document.querySelectorAll(selector);
      for (const element of elements) {
        if (isElementVisible(element) && element.src) {
          return element;
        }
      }
    }
    return Array.from(
      document.querySelectorAll('video:not([hidden]), img[src*="fbcdn"]:not([width="16"]):not([hidden])')
    ).find(el => {
      const rect = el.getBoundingClientRect();
      return isElementVisible(el) && rect.width > 150 && rect.height > 150 && el.src;
    });
  };

  const isInStoryOrReelView = () => {
    const url = window.location.href;
    if (
      url.includes("/stories/") ||
      url.includes("/reel/") ||
      url.includes("/videos/") ||
      url.includes("/watch/?") ||
      url.includes("/photo") ||
      url.includes("/photos/") ||
      url.includes("/highlights/")
    ) {
      return true;
    }
    for (const selector of SELECTORS.storyIndicators) {
      if (document.querySelector(selector)) {
        return true;
      }
    }
    return false;
  };

  const getBestVideoSource = (videoElement) => {
    try {
      const sources = Array.from(videoElement.querySelectorAll("source"))
        .map(s => ({
          url: s.src,
          width: parseInt(s.getAttribute("data-width") || s.getAttribute("width") || "0", 10),
          bitrateMatch: (s.src.match(/[?&](?:br|bitrate|vencode_tag)=(\d+)/) || [])[1]
        }))
        .filter(s => s.url);

      if (sources.length > 0) {
        sources.sort((a, b) => {
          const scoreA = a.width || parseInt(a.bitrateMatch || "0", 10);
          const scoreB = b.width || parseInt(b.bitrateMatch || "0", 10);
          return scoreB - scoreA;
        });
        if (sources[0].width || sources[0].bitrateMatch) return sources[0].url;
      }
    } catch (e) { /* fall through to default */ }

    return videoElement.currentSrc || videoElement.src;
  };

  const downloadMedia = (url) => {
    fetch(url)
      .then(response => response.blob())
      .then(blob => {
        if (window.DownloadBridge && window.DownloadBridge.downloadBase64File) {
          const reader = new FileReader();
          reader.onloadend = function() {
            if (reader.result) {
              window.DownloadBridge.downloadBase64File(
                reader.result,
                blob.type || "image/jpeg"
              );
            }
          };
          reader.readAsDataURL(blob);
        }
      })
      .catch(err => {
        console.error("Error downloading media:", err);
      });
  };

  const extractAndDownloadMedia = () => {
    const mediaElement = getCurrentMediaElement();

    if (mediaElement && mediaElement.src && mediaElement.src !== lastDownloadedUrl) {
      const bestUrl = mediaElement.tagName === "VIDEO"
        ? getBestVideoSource(mediaElement)
        : mediaElement.src;
      downloadMedia(bestUrl);
      lastDownloadedUrl = mediaElement.src;
      return;
    }

    const container = currentContentContainer || document.body;

    const videoElement = container.querySelector("video:not([hidden])");
    if (videoElement && videoElement.src && videoElement.src !== lastDownloadedUrl) {
      downloadMedia(getBestVideoSource(videoElement));
      lastDownloadedUrl = videoElement.src;
      return;
    }

    const images = Array.from(container.querySelectorAll("img"))
      .filter(img =>
        img.src &&
        !img.src.includes("data:image") &&
        img.src !== lastDownloadedUrl
      )
      .filter(img => {
        const rect = img.getBoundingClientRect();
        return rect.width >= 100 && rect.height >= 100 && isElementVisible(img);
      })
      .sort((a, b) => {
        const areaA = a.getBoundingClientRect().width * a.getBoundingClientRect().height;
        const areaB = b.getBoundingClientRect().width * b.getBoundingClientRect().height;
        return areaB - areaA;
      });

    if (images.length > 0) {
      downloadMedia(images[0].src);
      lastDownloadedUrl = images[0].src;
      return;
    }

    const backgroundElements = Array.from(container.querySelectorAll("*"));

    for (const el of backgroundElements) {
      const style = window.getComputedStyle(el);
      const bgImage = style.backgroundImage;

      if (
        bgImage &&
        bgImage !== "none" &&
        (bgImage.includes("fbcdn.net") || bgImage.includes("fbsbx.com"))
      ) {
        const imageUrl = bgImage.replace(/^url\(['"](.+)['"]\)$/, "$1");

        if (imageUrl !== lastDownloadedUrl) {
          downloadMedia(imageUrl);
          lastDownloadedUrl = imageUrl;
          return;
        }
      }
    }

    debugLog("No media content found to download");
  };

  const createDownloadButton = () => {
    const css = `
      #${'$'}{DOWNLOAD_BTN_ID} {
        position: fixed;
        top: 70px;
        right: 15px;
        width: 40px;
        height: 40px;
        background-color: rgba(0, 0, 0, 0.7);
        color: white;
        border-radius: 50%;
        z-index: ${'$'}{CONFIG.buttonZIndex};
        border: none;
        display: none;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        box-shadow: 0 2px 5px rgba(0,0,0,0.3);
        cursor: pointer;
        background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 960 960" fill="white"><path d="M480,640L280,440L336,384L440,488L440,160L520,160L520,488L624,384L680,440L480,640ZM240,800Q207,800 183.5,776.5Q160,753 160,720L160,600L240,600L240,720Q240,720 240,720Q240,720 240,720L720,720Q720,720 720,720Q720,720 720,720L720,600L800,600L800,720Q800,753 776.5,776.5Q753,800 720,800L240,800Z"/></svg>');
        background-repeat: no-repeat;
        background-position: center;
        background-size: 24px;
      }
      #${'$'}{DOWNLOAD_BTN_ID}.visible {
        display: flex !important;
      }
    `;

    const style = document.createElement("style");
    style.textContent = css;
    document.head.appendChild(style);

    const btn = document.createElement("button");
    btn.id = DOWNLOAD_BTN_ID;
    btn.setAttribute("aria-label", "Download content");

    btn.addEventListener("click", () => {
      currentContentContainer = null;
      lastDownloadedUrl = null;

      const mediaElement = getCurrentMediaElement();
      if (mediaElement) {
        currentContentContainer = findContentContainer(mediaElement);
      }

      extractAndDownloadMedia();
    });

    document.body.appendChild(btn);

    return btn;
  };

  const hideOpenAppButtons = (root = document) => {
    const buttons = root.querySelectorAll('div[role="button"]');
    buttons.forEach(button => {
      const flAcDiv = button.querySelector('div.fl.ac');
      if (flAcDiv) {
        const span = flAcDiv.querySelector('span');
        if (span && span.textContent.includes('\u{F196C}')) {
          button.style.display = 'none';
        }
      }
    });
  };

  const isFeed = () => {
    return document.querySelector('div[role="feed"]') !== null &&
           !isInStoryOrReelView();
  };

  const updateButtonVisibility = () => {
    let btn = document.getElementById(DOWNLOAD_BTN_ID);
    if (!btn) btn = createDownloadButton();

    if (isInStoryOrReelView() && !isFeed()) {
      const mediaElement = getCurrentMediaElement();
      hideOpenAppButtons();

      if (mediaElement) {
        currentContentContainer = findContentContainer(mediaElement);
        btn.classList.add("visible");
        return;
      }

      const highlightedStoryContainer = document.querySelector(
        'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"]'
      );

      if (highlightedStoryContainer) {
        const mediaInHighlight = highlightedStoryContainer.querySelector(
          'video, img[src*="fbcdn"]'
        );

        if (mediaInHighlight && isElementVisible(mediaInHighlight)) {
          currentContentContainer = highlightedStoryContainer;
          btn.classList.add("visible");
          return;
        }
      }
    }

    btn.classList.remove("visible");
    currentContentContainer = null;
  };

  const processPage = () => {
    if (isProcessing) return;
    isProcessing = true;
    try {
      updateButtonVisibility();
    } finally {
      isProcessing = false;
    }
  };

  const init = () => {
    currentContentContainer = null;
    lastDownloadedUrl = null;
    processPage();

    const observer = new MutationObserver(mutations => {
      const hasRelevantChanges = mutations.some(
        mutation =>
          (mutation.type === "childList" && mutation.addedNodes.length > 0) ||
          (mutation.type === "attributes" &&
            (mutation.target.tagName === "VIDEO" ||
             mutation.target.tagName === "IMG"))
      );
      if (hasRelevantChanges) processPage();
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ["src", "style", "class"]
    });
  };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
"""

private const val UX_EXTRAS_SCRIPT = """
/*
 * UX extras: hide "get the app" banners, add native video controls,
 * simple double-click-to-zoom image magnifier.
 */
(function () {
  try {
    if (window.__nobookUxExtrasActive) return;
    window.__nobookUxExtrasActive = true;

    var hideAppBanners = function () {
      document.querySelectorAll('div[role="button"]').forEach(function (btn) {
        var label = (btn.getAttribute('aria-label') || btn.textContent || '').toLowerCase();
        if (label.indexOf('use app') !== -1 || label.indexOf('get app') !== -1 ||
            label.indexOf('open in app') !== -1 || label.indexOf('mo trong app') !== -1 ||
            label.indexOf('tai app') !== -1) {
          var container = btn.closest('div[role="dialog"]') || btn.parentElement;
          if (container) container.style.display = 'none';
        }
      });
      document.querySelectorAll('a[href*="itunes.apple.com"], a[href*="play.google.com/store"]').forEach(function (a) {
        var wrap = a.closest('div[role="dialog"]') || a.parentElement;
        if (wrap) wrap.style.display = 'none';
      });
    };

    var addVideoControls = function () {
      document.querySelectorAll('video').forEach(function (v) {
        if (!v.hasAttribute('controls')) {
          v.setAttribute('controls', 'controls');
          v.controls = true;
        }
      });
    };

    var MAGNIFIER_ID = 'nobook-image-magnifier-overlay';
    var closeMagnifier = function () {
      var el = document.getElementById(MAGNIFIER_ID);
      if (el) el.remove();
    };
    var openMagnifier = function (src) {
      closeMagnifier();
      var overlay = document.createElement('div');
      overlay.id = MAGNIFIER_ID;
      overlay.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,0.9);z-index:999998;' +
        'display:flex;align-items:center;justify-content:center;';
      overlay.addEventListener('click', closeMagnifier);
      var img = document.createElement('img');
      img.src = src;
      img.style.cssText = 'max-width:95%;max-height:95%;object-fit:contain;';
      img.addEventListener('click', function (e) { e.stopPropagation(); });
      overlay.appendChild(img);
      document.body.appendChild(overlay);
    };
    var bindImageMagnifier = function () {
      document.querySelectorAll('img[src*="fbcdn"]').forEach(function (img) {
        if (img.dataset.nobookMagnifierBound) return;
        img.dataset.nobookMagnifierBound = '1';
        img.addEventListener('dblclick', function (e) {
          e.preventDefault();
          openMagnifier(img.currentSrc || img.src);
        });
      });
    };

    var runAll = function () {
      hideAppBanners();
      addVideoControls();
      bindImageMagnifier();
    };

    runAll();
    var observer = new MutationObserver(function () { runAll(); });
    observer.observe(document.body, { childList: true, subtree: true });

    console.info('[Nobook] UX extras active');
  } catch (err) {
    console.error('[Nobook] UX extras injection failed:', err);
  }
})();
"""

@Composable
fun NobookWebView(
    url: String,
    settingsVM: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val resources = LocalResources.current

    val state = rememberSaveableWebViewState(url)
    val navigator = rememberWebViewNavigator(
        requestInterceptor = ExternalRequestInterceptor { externalUrl ->
            if (isBlockedSite(externalUrl)) {
                Toast.makeText(
                    context,
                    "Nobook: da chan link nay theo danh sach blocklist",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val cleanUrl = sanitizeTrackingParams(externalUrl)
                val intent = Intent(Intent.ACTION_VIEW, cleanUrl.toUri())
                runCatching {
                    context.startActivity(intent)
                }.onFailure {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.not_supported),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    )

    LaunchedEffect(navigator) {
        val bundle = state.viewState
        if (bundle == null) {
            navigator.loadUrl(url)
        }
    }

    // allow exiting while scrolling to top.
    var exitScroll by remember { mutableStateOf(false) }
    BackHandler {
        if (exitScroll) {
            activity?.finish()
        } else {
            navigator.evaluateJavaScript("backHandlerNB();") {
                val backHandled = it.removeSurrounding("\"")
                when (backHandled) {
                    "false" -> {
                        if (navigator.canGoBack) {
                            navigator.navigateBack()
                        } else {
                            activity?.finish()
                        }
                    }
                    "exit" -> activity?.finish()
                    "scrolling" -> exitScroll = true
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

    val isDesktop by settingsVM.desktopLayout.collectAsState()
    val isAutoRevert by settingsVM.isRevertDesktop.collectAsState()
    val isAutoDesktop = rememberAutoDesktop()

    LaunchedEffect(Unit) {
        if (isAutoDesktop && !isDesktop) {
            settingsVM.setRevertDesktop(true)
            settingsVM.setDesktopLayout(true)
        }
        else if (!isAutoDesktop && isAutoRevert) {
            settingsVM.setRevertDesktop(false)
            settingsVM.setDesktopLayout(false)
        }
    }

    var isLoading by rememberSaveable { mutableStateOf(true) }
    val isError = state.errorsForCurrentRequest.lastOrNull()?.isFromMainFrame == true

    val viewModel: MainViewModel = viewModel {
        MainViewModel(
            resources = resources,
            settings = settingsVM
        )
    }

    val themeColor by viewModel.themeColor
    // Manual handling to fix visual & padding bug on settings dialog.
    var isImmersiveMode by rememberSaveable { mutableStateOf(settingsVM.immersiveMode.value) }

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

    val userScripts by viewModel.scripts
    val loadingState = state.loadingState

    LaunchedEffect(loadingState, userScripts) {
        if (loadingState is LoadingState.Finished) {
            userScripts?.let { scripts ->
                navigator.evaluateJavaScript(scripts) {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(loadingState) {
        if (loadingState is LoadingState.Finished) {
            navigator.evaluateJavaScript(ANTI_RELOAD_SCRIPT) {}
        }
    }

    LaunchedEffect(loadingState) {
        if (loadingState is LoadingState.Finished) {
            navigator.evaluateJavaScript(STORY_REEL_DOWNLOADER_SCRIPT) {}
        }
    }

    LaunchedEffect(loadingState) {
        if (loadingState is LoadingState.Finished) {
            navigator.evaluateJavaScript(UX_EXTRAS_SCRIPT) {}
        }
    }

    if (isError && isLoading) {
        NetworkErrorDialog { activity?.finish() }
        return
    }

    var settingsToggle by rememberSaveable { mutableStateOf(false) }
    if (settingsToggle) {
        setWindow(false)
        SettingsDialog(
            themeColor = themeColor,
            onDismiss = {
                setWindow(settingsVM.immersiveMode.value)
                settingsToggle = false
            },
            onReload = {
                isLoading = true
                viewModel.setThemeColor(Color.Transparent)
                setWindow(settingsVM.immersiveMode.value)
                viewModel.refresh(
                    resources = resources,
                    settings = settingsVM
                )
                navigator.reload()
            }
        )
    }

    if (isLoading) {
        SplashLoading(
            if (loadingState is LoadingState.Loading) {
                loadingState.progress
            } else {
                0.8F
            }
        )
    }


    LaunchedEffect(isDesktop) {
        val userAgent = if (isDesktop) DESKTOP_USER_AGENT else ""
        state.nativeWebView.settings.userAgentString = userAgent
    }

    // needed to consume extra padding when keyboard is open
    val barsInsets = WindowInsets.systemBars.asPaddingValues()
    val imeHeight = rememberImeHeight()

    WebView(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColor)
            .then(
                if (isImmersiveMode) {
                    Modifier.padding(bottom = imeHeight)
                } else {
                    Modifier.padding(
                        top = barsInsets.calculateTopPadding(),
                        bottom = maxOf(barsInsets.calculateBottomPadding(), imeHeight)
                    )
                }
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
                addJavascriptInterface(
                    DownloadBridge(context),
                    "DownloadBridge"
                )
                addJavascriptInterface(
                    ClipboardBridge(context),
                    "ClipboardBridge"
                )

                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                overScrollMode = View.OVER_SCROLL_NEVER
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false

                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
            }
        }
    )
}
