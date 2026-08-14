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

private fun isMessengerAppDeepLink(url: String): Boolean {
    val lower = url.lowercase()
    return lower.startsWith("fb-messenger://") ||
        (lower.startsWith("intent://") && lower.contains("messenger")) ||
        lower.contains("com.facebook.orca") ||
        (lower.startsWith("market://details") && lower.contains("orca"))
}

private const val STORY_REEL_DOWNLOADER_SCRIPT = """
(function() {
  var HOLD_TO_REVEAL_MS = 5000;
  var BTN_LONGPRESS_MS = 700;
  var AUTO_HIDE_MS = 5000;
  var holdTimer = null;
  var activeBtn = null;
  var hideTimer = null;

  function isMediaEligible(el) {
    if (!el) return false;
    if (el.tagName === "VIDEO") return true;
    if (el.tagName === "IMG" && el.src && el.src.indexOf("fbcdn") !== -1) return true;
    return false;
  }

  function findMediaTarget(start) {
    var node = start;
    while (node && node !== document.body) {
      if (isMediaEligible(node)) return node;
      if (node.querySelector) {
        var v = node.querySelector("video");
        if (v) return v;
        var img = node.querySelector('img[src*="fbcdn"]');
        if (img) return img;
      }
      node = node.parentElement;
    }
    return null;
  }

  function extractPlayableUrlFromPage() {
    try {
      var html = document.documentElement.innerHTML;
      var patterns = [
        /"browser_native_hd_url":"([^"]+)"/,
        /"browser_native_sd_url":"([^"]+)"/,
        /"playable_url_quality_hd":"([^"]+)"/,
        /"playable_url":"([^"]+)"/
      ];
      for (var i = 0; i < patterns.length; i++) {
        var m = html.match(patterns[i]);
        if (m && m[1]) {
          return m[1].split("\\/").join("/").split("\\u0025").join("%");
        }
      }
    } catch (e) {}
    return null;
  }

  function getBestVideoSource(videoElement) {
    try {
      var sourceEls = videoElement.querySelectorAll("source");
      var best = null, bestScore = -1;
      for (var i = 0; i < sourceEls.length; i++) {
        var s = sourceEls[i];
        if (!s.src) continue;
        var width = parseInt(s.getAttribute("data-width") || s.getAttribute("width") || "0", 10);
        var bm = s.src.match(/[?&](?:br|bitrate|vencode_tag)=(\d+)/);
        var score = width || (bm ? parseInt(bm[1], 10) : 0);
        if (score > bestScore) { bestScore = score; best = s.src; }
      }
      if (best) return best;
    } catch (e) {}
    var candidate = videoElement.currentSrc || videoElement.src;
    if (!candidate || candidate.indexOf("blob:") === 0) {
      var fb = extractPlayableUrlFromPage();
      if (fb) return fb;
    }
    return candidate;
  }

  function downloadMedia(url) {
    if (!url || url.indexOf("blob:") === 0) {
      console.error("[Nobook] Cannot download blob/empty URL directly:", url);
      return;
    }
    fetch(url).then(function(r) { return r.blob(); }).then(function(blob) {
      if (window.DownloadBridge && window.DownloadBridge.downloadBase64File) {
        var reader = new FileReader();
        reader.onloadend = function() {
          if (reader.result) {
            window.DownloadBridge.downloadBase64File(reader.result, blob.type || "image/jpeg");
          }
        };
        reader.readAsDataURL(blob);
      }
    }).catch(function(err) { console.error("Error downloading media:", err); });
  }

  function downloadFromElement(mediaEl) {
    if (!mediaEl) return;
    if (mediaEl.tagName === "VIDEO") {
      downloadMedia(getBestVideoSource(mediaEl));
    } else if (mediaEl.src) {
      downloadMedia(mediaEl.src);
    } else {
      var fb = extractPlayableUrlFromPage();
      if (fb) downloadMedia(fb);
    }
  }

  function removeButton() {
    if (activeBtn) { activeBtn.parentNode.removeChild(activeBtn); activeBtn = null; }
    if (hideTimer) { clearTimeout(hideTimer); hideTimer = null; }
  }

  function showButtonFor(mediaEl) {
    removeButton();
    var rect = mediaEl.getBoundingClientRect();
    if (rect.width === 0 && rect.height === 0) return;
    var btn = document.createElement("button");
    btn.id = "nobook-contextual-downloader";
    var top = Math.max(rect.top + 8, 8);
    var left = Math.min(Math.max(rect.right - 48, 8), window.innerWidth - 48);
    btn.style.position = "fixed";
    btn.style.top = top + "px";
    btn.style.left = left + "px";
    btn.style.width = "42px";
    btn.style.height = "42px";
    btn.style.borderRadius = "50%";
    btn.style.border = "none";
    btn.style.zIndex = "999999";
    btn.style.backgroundColor = "rgba(0,0,0,0.75)";
    btn.style.color = "white";
    btn.style.fontSize = "20px";
    btn.style.boxShadow = "0 2px 6px rgba(0,0,0,0.4)";
    btn.textContent = "\u2B07";
    btn.setAttribute("aria-label", "Tai xuong (giu de chon thu muc luu)");

    var btnPressTimer = null;
    btn.addEventListener("click", function() {
      downloadFromElement(mediaEl);
      removeButton();
    });
    function startBtnLongPress() {
      btnPressTimer = setTimeout(function() {
        if (window.DownloadFolderBridge && window.DownloadFolderBridge.pickFolder) {
          window.DownloadFolderBridge.pickFolder();
        }
      }, BTN_LONGPRESS_MS);
    }
    function endBtnLongPress() { if (btnPressTimer) clearTimeout(btnPressTimer); }
    btn.addEventListener("touchstart", startBtnLongPress, { passive: true });
    btn.addEventListener("touchend", endBtnLongPress);
    btn.addEventListener("mousedown", startBtnLongPress);
    btn.addEventListener("mouseup", endBtnLongPress);

    document.body.appendChild(btn);
    activeBtn = btn;
    hideTimer = setTimeout(removeButton, AUTO_HIDE_MS);
  }

  function startHold(e) {
    if (e.target && e.target.id === "nobook-contextual-downloader") return;
    var target = findMediaTarget(e.target);
    if (!target) return;
    cancelHold();
    holdTimer = setTimeout(function() { showButtonFor(target); }, HOLD_TO_REVEAL_MS);
  }

  function cancelHold() {
    if (holdTimer) { clearTimeout(holdTimer); holdTimer = null; }
  }

  document.addEventListener("touchstart", startHold, { passive: true });
  document.addEventListener("touchend", cancelHold, { passive: true });
  document.addEventListener("touchmove", cancelHold, { passive: true });
  document.addEventListener("mousedown", startHold);
  document.addEventListener("mouseup", cancelHold);
  document.addEventListener("click", function(e) {
    if (activeBtn && e.target !== activeBtn) removeButton();
  }, true);

  console.info("[Nobook] Contextual media downloader active (hold 5s on photo/video)");
})();
"""

private const val UX_EXTRAS_SCRIPT = """
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

private const val SPONSORED_VI_SCRIPT = """
(function () {
  try {
    if (window.__nobookSponsoredViActive) return;
    window.__nobookSponsoredViActive = true;

    var VI_SPONSORED_KEYWORDS = [
      'được tài trợ',
      'duoc tai tro',
      'noi dung duoc tai tro',
      'nội dung được tài trợ'
    ];

    var normalize = function (text) {
      return (text || '').toLowerCase();
    };

    var isSponsoredLabel = function (text) {
      var norm = normalize(text);
      return VI_SPONSORED_KEYWORDS.some(function (kw) { return norm.indexOf(kw) !== -1; });
    };

    var hideSponsoredPosts = function () {
      var candidates = document.querySelectorAll(
        'span, a[role="link"], div[aria-label]'
      );
      candidates.forEach(function (el) {
        var label = el.getAttribute ? (el.getAttribute('aria-label') || '') : '';
        var text = el.textContent || '';
        if (isSponsoredLabel(label) || isSponsoredLabel(text)) {
          var post = el.closest('div[role="article"]') ||
                     el.closest('div[data-pagelet^="FeedUnit"]');
          if (post) {
            post.style.display = 'none';
          }
        }
      });
    };

    hideSponsoredPosts();
    var observer = new MutationObserver(function () { hideSponsoredPosts(); });
    observer.observe(document.body, { childList: true, subtree: true });

    console.info('[Nobook] Vietnamese sponsored-post filter active');
  } catch (err) {
    console.error('[Nobook] Vietnamese sponsored filter injection failed:', err);
  }
})();
"""

private const val TOPIC_KEYWORD_FILTER_SCRIPT = """
(function () {
  try {
    if (window.__nobookTopicFilterActive) return;
    window.__nobookTopicFilterActive = true;

    var KEYWORDS = [];

    if (KEYWORDS.length === 0) {
      console.info('[Nobook] Topic keyword filter loaded (no keywords configured)');
      return;
    }

    var normalize = function (text) {
      return (text || '').toLowerCase();
    };

    var matchesKeyword = function (text) {
      var norm = normalize(text);
      return KEYWORDS.some(function (kw) { return norm.indexOf(kw.toLowerCase()) !== -1; });
    };

    var filterFeed = function () {
      document.querySelectorAll('div[role="article"]').forEach(function (post) {
        if (post.dataset.nobookTopicChecked) return;
        var text = post.innerText || '';
        if (matchesKeyword(text)) {
          post.style.display = 'none';
        }
        post.dataset.nobookTopicChecked = '1';
      });
    };

    filterFeed();
    var observer = new MutationObserver(function () { filterFeed(); });
    observer.observe(document.body, { childList: true, subtree: true });

    console.info('[Nobook] Topic keyword filter active (' + KEYWORDS.length + ' keywords)');
  } catch (err) {
    console.error('[Nobook] Topic keyword filter injection failed:', err);
  }
})();
"""

private const val NETWORK_SANITIZER_SCRIPT = """
(function () {
  try {
    if (window.__nobookNetworkSanitizerActive) return;
    window.__nobookNetworkSanitizerActive = true;

    var UI_SELECTORS_TO_REMOVE = [
      '[aria-label="Sponsored"]',
      '[data-testid="story-sponsored-label"]',
      '[data-ad-comet-preview-id]',
      '[data-adunit]',
      '[data-sigil="m-feed-voice-subtitle"]',
      'div[id^="ad_"]'
    ];

    var BLOCKED_NETWORK_PATTERNS = [
      /an\.facebook\.com/,
      /pixel\.facebook\.com/,
      /graph\.facebook\.com\/v\d+\/\d+\/activities/,
      /audience_network/
    ];

    var sanitizeDOM = function () {
      UI_SELECTORS_TO_REMOVE.forEach(function (sel) {
        try {
          document.querySelectorAll(sel).forEach(function (el) {
            var root = el.closest('div[role="article"]') || el.closest('[data-pagelet]') || el;
            root.style.display = 'none';
          });
        } catch (e) { /* ignore selector errors */ }
      });
    };

    var origXhrOpen = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function (method, url) {
      for (var i = 0; i < BLOCKED_NETWORK_PATTERNS.length; i++) {
        if (BLOCKED_NETWORK_PATTERNS[i].test(url)) {
          console.info('[Nobook] Blocked tracking XHR:', url);
          arguments[1] = 'about:blank';
          break;
        }
      }
      return origXhrOpen.apply(this, arguments);
    };

    var origFetch = window.fetch;
    window.fetch = function (input, init) {
      var url = (typeof input === 'string') ? input : (input && input.url) || '';
      for (var i = 0; i < BLOCKED_NETWORK_PATTERNS.length; i++) {
        if (BLOCKED_NETWORK_PATTERNS[i].test(url)) {
          console.info('[Nobook] Blocked tracking fetch:', url);
          return Promise.resolve(new Response('{}', { status: 200 }));
        }
      }
      return origFetch.apply(window, arguments);
    };

    sanitizeDOM();
    var observer = new MutationObserver(function () { sanitizeDOM(); });
    observer.observe(document.body, { childList: true, subtree: true });
    setInterval(sanitizeDOM, 3000);

    console.info('[Nobook] Network/DOM sanitizer active');
  } catch (err) {
    console.error('[Nobook] Network sanitizer injection failed:', err);
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

    val folderPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            context.getSharedPreferences("nobook_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("download_folder_uri", uri.toString())
                .apply()
            Toast.makeText(context, "Da chon thu muc luu tai xuong moi", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        DownloadFolderPicker.onPickRequested = { folderPickerLauncher.launch(null) }
        onDispose { DownloadFolderPicker.onPickRequested = null }
    }

    val state = rememberSaveableWebViewState(url)
    val navigator = rememberWebViewNavigator(
        requestInterceptor = ExternalRequestInterceptor { externalUrl ->
            if (isMessengerAppDeepLink(externalUrl)) {
                // Stay inside the WebView: do not jump to the native Messenger
                // app or the Play Store install prompt.
            } else if (isBlockedSite(externalUrl)) {
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

    LaunchedEffect(loadingState) {
        if (loadingState is LoadingState.Finished) {
            navigator.evaluateJavaScript(SPONSORED_VI_SCRIPT) {}
        }
    }

    LaunchedEffect(loadingState) {
        if (loadingState is LoadingState.Finished) {
            navigator.evaluateJavaScript(TOPIC_KEYWORD_FILTER_SCRIPT) {}
        }
    }

    LaunchedEffect(loadingState) {
        if (loadingState is LoadingState.Finished) {
            navigator.evaluateJavaScript(NETWORK_SANITIZER_SCRIPT) {}
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
                    DownloadFolderBridge(context),
                    "DownloadFolderBridge"
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
