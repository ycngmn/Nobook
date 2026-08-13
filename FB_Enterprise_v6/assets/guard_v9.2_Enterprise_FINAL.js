/**
 * guard_v9.2_Enterprise_FINAL.js
 * FB Enterprise AdBlocker v8.0 — WebView JS Guard Layer
 * Injected into Facebook WebView on every page load via ModInit$WebViewClientHook
 * 
 * Features:
 *  - DOM-based ad node removal (Sponsored, Suggested, Audience Network banners)
 *  - MutationObserver for dynamic feed ad injection
 *  - XHR/Fetch intercept for ad analytics requests
 *  - Video URL capture → calls window.VideoJsInterface.triggerDownload()
 *  - Cookie export trigger → calls window.CookieJsInterface.exportCookies()
 */

(function(window, document) {
    'use strict';

    const VERSION = '9.2-Enterprise-FINAL';
    const LOG_PREFIX = '[FBGuard v9.2]';

    // ── 1. AD SELECTOR CONFIG ─────────────────────────────
    const AD_SELECTORS = [
        '[data-pagelet*="FeedUnit"]',
        '[aria-label="Sponsored"]',
        '[data-ad-comet-preview-id]',
        '[data-adunit]',
        '[data-testid="story-sponsored-label"]',
        '[role="feed"] [aria-label="Suggested for you"]',
        '[data-sigil="m-feed-voice-subtitle"]',
        'div[id^="ad_"]',
        'div[class*="sponsored"]',
        '[data-fte="1"]'
    ];

    // ── 2. AD BLOCK URL PATTERNS ─────────────────────────
    const BLOCKED_PATTERNS = [
        /an\.facebook\.com/,
        /audience_network/,
        /fbevents\.js/,
        /connect\.facebook\.net.*pixel/,
        /graph\.facebook\.com\/v\d+\/\d+\/activities/,
        /www\.facebook\.com\/ajax\/bz/,
        /pixel\.facebook\.com/,
        /facebook\.com\/adview/,
        /static\.xx\.fbcdn\.net.*sponsored/
    ];

    let stats = { blocked: 0, removed: 0, intercepted: 0 };

    // ── 3. DOM AD REMOVAL ─────────────────────────────────
    function removeAdNodes() {
        let count = 0;
        AD_SELECTORS.forEach(sel => {
            try {
                document.querySelectorAll(sel).forEach(el => {
                    // Double-check text content for "Sponsored" label
                    const text = el.innerText || '';
                    if (text.includes('Sponsored') || text.includes('Suggested') || sel.includes('Sponsored')) {
                        const root = el.closest('[data-pagelet]') || el;
                        root.remove();
                        count++;
                    }
                });
            } catch(e) { /* ignore */ }
        });
        if (count > 0) {
            stats.removed += count;
            console.log(LOG_PREFIX, `Removed ${count} ad nodes. Total: ${stats.removed}`);
        }
    }

    // ── 4. MUTATION OBSERVER – watch for dynamically injected ads ──
    function watchFeedMutations() {
        if (!window.MutationObserver) return;
        const observer = new MutationObserver(mutations => {
            let needsScan = false;
            mutations.forEach(m => {
                if (m.addedNodes.length > 0) needsScan = true;
            });
            if (needsScan) removeAdNodes();
        });
        const feedRoot = document.querySelector('[role="feed"]') || document.body;
        observer.observe(feedRoot, { childList: true, subtree: true });
        console.log(LOG_PREFIX, 'MutationObserver active on feed root.');
    }

    // ── 5. XHR INTERCEPT ─────────────────────────────────
    const _XHROpen = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function(method, url) {
        for (const pattern of BLOCKED_PATTERNS) {
            if (pattern.test(url)) {
                stats.intercepted++;
                console.log(LOG_PREFIX, `[XHR BLOCKED] ${url}`);
                // Abort silently
                return _XHROpen.call(this, method, 'about:blank');
            }
        }
        return _XHROpen.apply(this, arguments);
    };

    // ── 6. FETCH INTERCEPT ────────────────────────────────
    const _fetch = window.fetch;
    window.fetch = function(input, init) {
        const url = (typeof input === 'string') ? input : input.url;
        for (const pattern of BLOCKED_PATTERNS) {
            if (pattern.test(url)) {
                stats.intercepted++;
                console.log(LOG_PREFIX, `[FETCH BLOCKED] ${url}`);
                return Promise.resolve(new Response('', { status: 204 }));
            }
        }
        return _fetch.apply(window, arguments);
    };

    // ── 7. VIDEO URL CAPTURE → L8_VideoDownloader ─────────
    function captureVideoUrls() {
        document.querySelectorAll('video source, video[src]').forEach(el => {
            const src = el.src || el.getAttribute('src') || '';
            if (src && src.includes('.mp4') && window.VideoJsInterface) {
                const filename = 'fb_video_' + Date.now() + '.mp4';
                try {
                    window.VideoJsInterface.triggerDownload(src, filename);
                    console.log(LOG_PREFIX, '[VIDEO CAPTURED]', src);
                } catch(e) { /* VideoJsInterface not attached */ }
            }
        });
    }

    // ── 8. COOKIE EXPORT HELPER ───────────────────────────
    window.__fbGuardExportCookies = function() {
        if (window.CookieJsInterface) {
            window.CookieJsInterface.exportCookies();
            return 'Export triggered via L12_CookieExporter';
        }
        return 'CookieJsInterface not available';
    };

    // ── 9. BOOTSTRAP ──────────────────────────────────────
    function init() {
        console.log(LOG_PREFIX, `guard_v9.2 initialized | Version: ${VERSION}`);
        removeAdNodes();
        watchFeedMutations();
        captureVideoUrls();

        // Periodic re-scan (every 3s for dynamic content)
        setInterval(() => {
            removeAdNodes();
            captureVideoUrls();
        }, 3000);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // ── 10. EXPOSE STATS TO NATIVE ────────────────────────
    window.__fbGuardStats = function() {
        return JSON.stringify(stats);
    };

})(window, document);
