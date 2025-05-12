
// Enable press and hold caption selection.
(function () {
  const applySelectableStyle = (el) => {
    if (el.closest('div[role="button"]')) return;
    el.style.setProperty('user-select', 'text', 'important');
    el.style.setProperty('-webkit-user-select', 'text', 'important');
    el.style.setProperty('-moz-user-select', 'text', 'important');
    el.style.setProperty('-ms-user-select', 'text', 'important');
    el.style.setProperty('pointer-events', 'auto', 'important');
    el.style.setProperty('touch-action', 'manipulation', 'important');
  };

  document.querySelectorAll('.native-text').forEach(applySelectableStyle);

  const observer = new MutationObserver((mutations) => {
    for (const mutation of mutations) {
      mutation.addedNodes.forEach((node) => {
        if (node.nodeType === 1) {
          if (node.classList.contains('native-text')) {
            applySelectableStyle(node);
          }
          node.querySelectorAll?.('.native-text').forEach(applySelectableStyle);
        }
      });
    }
  });

  observer.observe(document.body, {
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

// Hide facebook download button at login page.
(function() {
    const element = document.querySelector('div[data-bloks-name="bk.components.Flexbox"].wbloks_1');
    if (element) element.remove();
})();

// Hide annoying bottom banners
const observer = new MutationObserver(() => {

  if (location.pathname === '/'
  && document.querySelector('div[role="button"][aria-label*="Facebook"]') === null) return;

  const element = document.querySelector('.bottom.fixed-container');
  if (
    element &&
    !element.hasAttribute('data-shift-on-keyboard-shown')
  ) {
    const heightAttr = element.getAttribute('data-actual-height');
    if (heightAttr && parseInt(heightAttr, 10) < 80) {
      element.style.display = 'none';
    }
  }
});

observer.observe(document.body, { childList: true, subtree: true });


// Hold Effect Script
(function() {
  const style = document.createElement('style');
  style.innerHTML = '* { -webkit-tap-highlight-color: rgba(180, 180, 180, 0.35); }';
  document.head.appendChild(style);
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
     const hostname = window.location.hostname;
     const pathname = window.location.pathname;

     const isHomepage =
       (hostname === 'www.facebook.com'
       || hostname === 'm.facebook.com') &&
       (pathname === '/' || pathname === '');
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