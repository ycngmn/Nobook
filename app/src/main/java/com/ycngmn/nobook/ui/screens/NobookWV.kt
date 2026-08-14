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
 * Script to add a global download button for any visible video/image on
 * Facebook (feed, stories, reels, highlights, photo viewer).
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
      'div[data-pagelet="ProfilePhoto"] img[src*="fbcdn"]',
      'div[role="article"] video:not([hidden])'
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
      'div[data-pagelet*="ProfileAppSection"]',
      'div[role="article"]'
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
      rect.width > 0 && rect.height > 0 &&
      rect.bottom > 0 && rect.top < (window.innerHeight || document.documentElement.clientHeight) &&
      rect.right > 0 && rect.left < (window.innerWidth || document.documentElement.clientWidth)
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
      return isElementVisible(el) && rect.width > 100 && rect.height > 100 && el.src;
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

  const extractPlayableUrlFromPage = () => {
    try {
      const html = document.documentElement.innerHTML;
      const patterns = [
        /"browser_native_hd_url":"([^"]+)"/,
        /"browser_native_sd_url":"([^"]+)"/,
        /"playable_url_quality_hd":"([^"]+)"/,
        /"playable_url":"([^"]+)"/
      ];
      for (const p of patterns) {
        const m = html.match(p);
        if (m && m[1]) {
          return m[1].replace(/\\\//g, '/').replace(/\\u0025/g, '%');
        }
      }
    } catch (e) { /* ignore */ }
    return null;
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

    const candidate = videoElement.currentSrc || videoElement.src;
    if (!candidate || candidate.indexOf("blob:") === 0) {
      const fallback = extractPlayableUrlFromPage();
      if (fallback) return fallback;
    }
    return candidate;
  };

  const downloadMedia = (url) => {
    if (!url || url.indexOf("blob:") === 0) {
      console.error("[Nobook] Cannot download blob/empty URL directly:", url);
      return;
    }
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

    if (mediaElement && mediaElement.tagName === "VIDEO") {
      const bestUrl = getBestVideoSource(mediaElement);
      downloadMedia(bestUrl);
      lastDownloadedUrl = bestUrl;
      return;
    }

    if (mediaElement && mediaElement.src) {
      downloadMedia(mediaElement.src);
      lastDownloadedUrl = mediaElement.src;
      return;
    }

    const container = currentContentContainer || document.body;

    const videoElement = container.querySelector("video:not([hidden])");
    if (videoElement) {
      const bestUrl = getBestVideoSource(videoElement);
      downloadMedia(bestUrl);
      lastDownloadedUrl = bestUrl;
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
        downloadMedia(imageUrl);
        lastDownloadedUrl = imageUrl;
        return;
      }
    }

    const fallback = extractPlayableUrlFromPage();
    if (fallback) {
      downloadMedia(fallback);
      lastDownloadedUrl = fallback;
      return;
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

      const mediaElement = getCurrentMediaElement();
      if (mediaElement) {
        currentContentContainer = findContentContainer(mediaElement);
      }

      extractAndDownloadMedia();
    });

    let pressTimer = null;
    btn.addEventListener("touchstart", () => {
      pressTimer = setTimeout(() => {
        if (window.DownloadFolderBridge && window.DownloadFolderBridge.pickFolder) {
          window.DownloadFolderBridge.pickFolder();
        }
      }, 700);
    }, { passive: true });
    btn.addEventListener("touchend", () => { if (pressTimer) clearTimeout(pressTimer); });

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

  const updateButtonVisibility = () => {
    let btn = document.getElementById(DOWNLOAD_BTN_ID);
    if (!btn) btn = createDownloadButton();

    hideOpenAppButtons();

    const mediaElement = getCurrentMediaElement();
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

    setInterval(processPage, 1000);

    window.addEventListener("scroll", () => {
      requestAnimationFrame(processPage);
    }, { passive: true });
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

private const val SPONSORED_VI_SCRIPT = """
/*
 * Vietnamese sponsored-post keyword filter, complements existing adblock.js.
 * Hides post containers whose "Sponsored" label appears in Vietnamese.
 */
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
/*
 * Topic keyword filter: hides feed posts whose text matches user-defined
 * keywords/phrases (equivalent to FBPurity's "Text Filter"). Edit the
 * KEYWORDS list below to add topics you want hidden from your feed.
 */
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
/*
 * Client-side network/DOM sanitizer: removes sponsored-post UI elements
 * and blocks known Facebook tracking/telemetry endpoints from within the
 * page's own JS context (fetch/XHR patch). This runs entirely inside the
 * WebView's JS engine -- it does NOT touch TLS/certificate validation and
 * does NOT intercept native network traffic.
 */
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
