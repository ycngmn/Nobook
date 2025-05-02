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

// Hide facebook download button at login page.
(function() {
    const element = document.querySelector('div[data-bloks-name="bk.components.Flexbox"].wbloks_1[style*="flex-grow: 1; justify-content: inherit; flex-direction: column;"]');
    if (element) {
        element.remove();
    }
})();

// Hide Open With App Banner Script
(function() {
  const style = document.createElement('style');
  style.textContent = '.bottom[data-actual-height="67"] { display: none !important; }';
  document.head.appendChild(style);
})();

// Remove black overlay on search page
(() => {
  const obs = new MutationObserver(() => {
    const el = document.querySelector('.m.bg-s2.vscroller');
    if (el) {
      el.setAttribute('style', '');
    }
  });
  obs.observe(document.documentElement, { childList: true, subtree: true });
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

            const isHomepage = window.location.pathname === '/';
            const exists = document.querySelector('div[role="button"][aria-label*="Facebook"]') !== null;
            
            if (isHomepage && exists) scroller.style.paddingBottom = '0';
            
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

// Nobook settings : floating button
(function() {
  const btn = document.createElement('button');
  btn.innerHTML = '⚙️';
  Object.assign(btn.style, {
    position: 'fixed',
    top: '4px',
    right: '98px',
    width: '36px',
    height: '36px',
    backgroundColor: 'transparent',
    color: 'white',
    fontSize: '23px',
    fontWeight: 'bold',
    border: 'none',
    borderRadius: '50%',
    zIndex: '999999',
    cursor: 'pointer',
    alignItems: 'center',
    justifyContent: 'center',
    opacity: '1',
    display: 'none'
  });

  btn.onclick = () => {
    if (typeof SettingsBridge !== 'undefined' && SettingsBridge.onSettingsToggle) {
      SettingsBridge.onSettingsToggle();
    }
  };

  if (document.body) {
    document.body.appendChild(btn);
  } else {
    document.addEventListener('DOMContentLoaded', () => {
      document.body.appendChild(btn);
    });
  }

 // Feed identifier. To not show anywhere else than the feed.
 function checkAndToggleButton() {
   const isHomepage = window.location.pathname === '/';
   const exists = document.querySelector('div[role="button"][aria-label*="Facebook"]') !== null;
   btn.style.display = (exists && isHomepage) ? 'flex' : 'none';
 }

  const observer = new MutationObserver(checkAndToggleButton);

  observer.observe(document.body, { childList: true, subtree: true });

  checkAndToggleButton();
})();


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