/*
    MIT License

    Copyright (c) 2025 ycngmn

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/


/*
  Script Catalog
  ==============
  This file contains multiple scripts for enhancing and modifying the Facebook mobile experience.
  Each script is wrapped in an IIFE to prevent variable conflicts and is described below.

  * Sponsored Ad Blocker Script
     - Purpose: Blocks sponsored content on the page.
     - Functions:
       - blockSponsoredContent(config): Identifies and hides elements containing sponsored text based on provided configuration.
       - blockAllAds(): Applies ad blocking using predefined configurations for different content types.
       - Uses MutationObserver to continuously monitor and block new sponsored content.

  * File Download Script
     - Purpose: Intercepts file downloads and converts blobs to base64 for custom download handling.
     - Functions:
       - Overrides URL.createObjectURL to read blobs as base64 and trigger DownloadBridge.downloadBase64File.
       - Prevents reinitialization using a window._downloadBridgeInitialized flag.

  * Zoom Disable Script
     - Purpose: Disables zooming on the page except for photo pages.
     - Functions:
       - applyViewportLock(): Sets viewport meta tag to disable zooming unless on facebook.com/photo.php.
       - Uses MutationObserver to reapply viewport settings if the head element changes.

   * Color Extraction Script
     - Purpose: Extracts and monitors theme color changes from meta tags.
     - Functions:
       - notify(): Sends theme color to ThemeBridge.onThemeColorChanged if available.
       - Uses MutationObserver to detect changes in the theme-color meta tag.

  * Enhance Loading Overlay Script
     - Purpose: Increases transparency of loading overlays.
     - Functions:
       - applyOverlayStyle(): Sets loading overlay background to rgba(0, 0, 0, 0.1).
       - Uses MutationObserver to apply styles to newly added overlays.

  * Hide Open With App Banner Script
     - Purpose: Hides the "Open with App" banner at the bottom of the mobile page.
     - Functions:
       - Creates a style element to set display: none for the banner (div.fixed-container.bottom[style*="height:67px"]).

  * Hold Effect Script
     - Purpose: Adds a grey overlay effect on elements when held or clicked, excluding during scrolling.
     - Functions:
       - removeEffects(): Clears hold effect and timers.
       - Event listeners for touchstart, touchmove, click, touchend, touchcancel to manage the effect.
       - Uses MutationObserver to ensure the style element persists.

  * Sticky Top Navbar Script
     - Purpose: Makes the top navbar and tab-bar sticky and adjusts content positioning.
     - Functions:
       - applyStyles(): Sets fixed positioning for navbar and tabbar, adjusts scroller padding/margins, and styles pull-to-refresh elements.
       - Uses MutationObserver and a timeout to reapply styles on page changes.

  Notes:
  - All scripts use MutationObservers for dynamic content handling.
  - Scripts are designed for the Facebook mobile site.
  - No external dependencies are required.
 */


// Sponsored Ad Blocker Script
(function() {
    const sponsoredTexts = [
        "Sponsored", "Gesponsert", "Sponsorlu", "Sponsorowane",
        "Ispoonsara godhameera", "Geborg", "Bersponsor", "Ditaja",
        "Disponsori", "Giisponsoran", "Sponzorováno", "Sponsoreret",
        "Publicidad", "May Sponsor", "Sponsorisée", "Oipytyvôva",
        "Ɗaukar Nayin", "Sponzorirano", "Uterwa inkunga", "Sponsorizzato",
        "Imedhaminiwa", "Hirdetés", "Misy Mpiantoka", "Gesponsord",
        "Sponset", "Patrocinado", "Sponsorizat", "Sponzorované",
        "Sponsoroitu", "Sponsrat", "Được tài trợ", "Χορηγούμενη",
        "Спонсорирано", "Спонзорирано", "Ивээн тэтгэсэн", "Реклама",
        "Спонзорисано", "במימון", "سپانسرڈ", "دارای پشتیبانی مالی",
        "ስፖንሰር የተደረገ", "प्रायोजित", "ተደረገ", "प", "স্পনসর্ড",
        "ਪ੍ਰਯੋਜਿਤ", "પ્રાયોજિત", "ପ୍ରାୟୋଜିତ", "செய்யப்பட்ட செய்யப்பட்ட",
        "చేయబడినది చేయబడినది", "ಪ್ರಾಯೋಜಿಸಲಾಗಿದೆ", "ചെയ്‌തത് ചെയ്‌തത്",
        "ලද ලද ලද", "สนับสนุน สนับสนุน รับ สนับสนุน สนับสนุน",
        "ကြော်ငြာ ကြော်ငြာ", "ឧបត្ថម្ភ ឧបត្ថម្ភ ឧបត្ថម្ភ", "광고",
        "贊助", "赞助内容", "広告", "സ്‌പോൺസർ ചെയ്‌തത്"
    ];

    const sponsoredRegex = new RegExp(sponsoredTexts.join('|'), 'i');

    function blockSponsoredContent(config = {}) {
        const {
            selector = 'div', 
            textSelector = 'div[data-mcomponent="TextArea"] .native-text > span', 
            hideMethod = 'hide', 
            validateContainer = () => true 
        } = config;

        const containers = document.querySelectorAll(selector);

        containers.forEach(container => {
            const spans = container.querySelectorAll(textSelector);
            for (const span of spans) {
                if (sponsoredRegex.test(span.textContent) && validateContainer(container)) {
                    if (hideMethod === 'reposition') {
                        container.style.position = 'absolute';
                        container.style.left = '-3000px';
                    } else {
                        container.style.display = 'none';
                    }
                    break;
                }
            }
        });
    }

    const filterConfig = {
        selector: 'div[data-type="vscroller"] div[data-tracking-duration-id]:has(> div[data-focusable="true"] div[data-mcomponent*="TextArea"] .native-text > span)',
        textSelector: '.native-text > span',
        hideMethod: 'reposition',
        validateContainer: () => true
    };

    const scriptConfig = {
        selector: 'div[data-status-bar-color] > div[data-mcomponent="MContainer"] > div[data-mcomponent="MContainer"]',
        textSelector: 'div[data-mcomponent="TextArea"] .native-text > span',
        hideMethod: 'hide',
        validateContainer: (container) => !container.querySelector('[data-tracking-duration-id]')
    };

    function blockAllAds() {
        blockSponsoredContent(filterConfig);
        blockSponsoredContent(scriptConfig);
    }

    blockAllAds();

    const observer = new MutationObserver(blockAllAds);
    observer.observe(document.body, { childList: true, subtree: true });
})();


// Zoom Disable Script
(function() {
    function applyViewportLock() {
        let viewport = document.querySelector('meta[name="viewport"]');
        if (!viewport) {
            viewport = document.createElement('meta');
            viewport.name = "viewport";
            document.head.appendChild(viewport);
        }
        // allow zooming on photos.
        if (!window.location.href.includes("facebook.com/photo.php"))
            viewport.content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no";
    }
    
    applyViewportLock();
   
    const observer = new MutationObserver(() => {
        applyViewportLock();
    });

    observer.observe(document.head || document.documentElement, {
        childList: true,
        subtree: true
    });
})();


// Enhance Loading Overlay Script
(function() {
    // Function to apply the background color
    function applyOverlayStyle() {
        const overlays = document.querySelectorAll('.loading-overlay');
        overlays.forEach(overlay => {
            overlay.style.backgroundColor = 'rgba(0, 0, 0, 0.1)';
        });
    }
    applyOverlayStyle();

    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.addedNodes.length) {
                applyOverlayStyle();
            }
        });
    });

    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
})();

// Hide Open With App Banner Script
(function() {
    const style = document.createElement('style');
    style.innerHTML = 'div.fixed-container.bottom[style*="height:67px"] { display: none !important; }';
    document.head.appendChild(style);
})();

// Hold Effect Script
(function() {
    const style = document.createElement('style');
    style.innerHTML = `
        * { -webkit-tap-highlight-color: transparent !important; }
        .fb-hold-effect {
            position: relative;
        }
        .fb-hold-effect::after {
            content: '';
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(180, 180, 180, 0.35);
            pointer-events: none;
            z-index: 9999;
        }
    `;
    document.head.appendChild(style);

    let holdTimer, heldElement, touchStartY = 0, isScrolling = false;
    const HOLD_DELAY = 125, SCROLL_THRESHOLD = 5;

    const removeEffects = () => {
        clearTimeout(holdTimer);
        if (heldElement) {
            heldElement.classList.remove('fb-hold-effect');
            heldElement = null;
        }
    };

    document.addEventListener('touchstart', e => {
        touchStartY = e.touches[0].clientY;
        isScrolling = false;
        removeEffects();
        heldElement = e.target;
        if (heldElement.tagName.toLowerCase() !== 'body') {
            holdTimer = setTimeout(() => {
                if (!isScrolling) heldElement.classList.add('fb-hold-effect');
            }, HOLD_DELAY);
        }
    }, true);

    document.addEventListener('touchmove', e => {
        if (Math.abs(e.touches[0].clientY - touchStartY) > SCROLL_THRESHOLD) {
            isScrolling = true;
            removeEffects();
        }
    }, true);

    document.addEventListener('click', e => {
        if (isScrolling) return;
        const target = e.target;
        if (target.tagName.toLowerCase() !== 'body') {
            target.classList.add('fb-hold-effect');
            setTimeout(() => target.classList.remove('fb-hold-effect'), 100);
        }
    }, true);

    ['touchend', 'touchcancel'].forEach(event => 
        document.addEventListener(event, removeEffects, true)
    );

    new MutationObserver(() => {
        if (!document.head.contains(style)) document.head.appendChild(style);
    }).observe(document.body, { childList: true, subtree: true });
})();

// Sticky Top Navbar Script
(function() {
    const applyStyles = () => {
        const navbar = document.querySelector('div[data-tti-phase="-1"][data-mcomponent="MContainer"][data-type="container"][data-focusable="true"].m');
        const tabbar = document.querySelector('div[role="tablist"][data-tti-phase="-1"][data-type="container"][data-mcomponent="MContainer"].m');
        const scroller = document.querySelector('div[data-type="vscroller"][data-is-pull-to-refresh-allowed="true"]');

        const hasLogo = navbar?.querySelector('div[aria-label*="Facebook"]');
        const hasFeed = tabbar?.querySelector('div[aria-label*="feed"]');

        const navbarHeight = navbar ? parseFloat(getComputedStyle(navbar).height) || parseFloat(navbar.style.height) || 0 : 0;
        const tabbarHeight = tabbar ? parseFloat(getComputedStyle(tabbar).height) || parseFloat(tabbar.style.height) || 0 : 0;

        if (hasLogo) Object.assign(navbar.style, {
            position: 'fixed',
            top: '0',
            left: '0',
            width: '100%',
            zIndex: '1000',
            pointerEvents: 'auto'
        });

        if (hasFeed) Object.assign(tabbar.style, {
            position: 'fixed',
            top: hasLogo ? navbarHeight + 'px' : '',
            left: '0',
            width: '100%',
            zIndex: '999',
            pointerEvents: 'auto'
        });

        if (scroller) {
            const offset = (hasLogo ? navbarHeight : 0) + (hasFeed ? tabbarHeight : 0);
            const scrollContent = scroller.querySelector(':scope > div:not(.pull-to-refresh-spinner-container)');
            scrollContent ? scrollContent.style.marginTop = offset + 'px' : scroller.style.paddingTop = offset + 'px';
            
            if (!window.location.href.includes("facebook.com/story.php")) scroller.style.paddingBottom = '0';
            
            const spinnerContainer = scroller.querySelector('.pull-to-refresh-spinner-container');
            if (spinnerContainer) Object.assign(spinnerContainer.style, {
                zIndex: '1001',
            });

            const spinner = scroller.querySelector('.pull-to-refresh-spinner');
            if (spinner) spinner.style.margin = '0 auto';
        }

        Object.assign(document.body.style, {
            paddingTop: '0',
            marginTop: '0',
            overflow: 'visible',
            height: '100%'
        });
    };

    applyStyles();
    setTimeout(applyStyles, 2000);
    new MutationObserver(applyStyles).observe(document.body, { childList: true, subtree: true });
})();

/* The below scripts are specific to com.ycngmn.Nobook application. */

// Color Extraction Script
(function() {
    const meta = document.querySelector('meta[name="theme-color"]');
    const notify = () => window.ThemeBridge?.onThemeColorChanged?.(meta?.content ?? "null");
    if (meta) {
        notify();
        new MutationObserver(() => notify())
            .observe(meta, { attributes: true, attributeFilter: ['content'] });
    }
})();

// File Download Script
(function() {
    if (window._downloadBridgeInitialized) return;
    window._downloadBridgeInitialized = true;
    const originalCreateObjectURL = URL.createObjectURL;
    URL.createObjectURL = function(blob) {
        const reader = new FileReader();
        reader.onloadend = function() {
            if (reader.result) {
                DownloadBridge.downloadBase64File(reader.result, blob.type);
            }
        };
        reader.readAsDataURL(blob);
        return originalCreateObjectURL(blob);
    };
})();