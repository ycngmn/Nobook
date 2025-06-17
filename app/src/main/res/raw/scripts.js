// Feed identifier
(() => {
    window.isFeed = () => {
        return window.location.pathname === '/' &&
          (window.location.hostname === 'm.facebook.com' || window.location.hostname === 'www.facebook.com')
          && document.querySelector('div[role="button"][aria-label*="Facebook"]') !== null;
    }
})();

// Scroll to top on back-press at feed.
(() => {
    window.backHandlerNB = () => {
    const isDialog = document.querySelector('div[role="dialog"]');
    if (window.isFeed() && !isDialog) {
       if (window.scrollY !== 0) {
          // to interrupt any current scroll event.
          document.body.style.overflow = 'hidden';
          setTimeout(() => {
             document.body.style.overflow = '';
             window.scrollTo({ top: 0, behavior: 'smooth' });
          }, 50);
          return "true";
       } else return "exit";
    } else return "false"; }
})();

// Enable press and hold caption selection and apply custom selection color.
(() => {
  const makeSelectable = (el) => {
    if (el.closest('div[role="button"]')) return;
    el.style.userSelect = 'text';
    el.style.pointerEvents = 'auto';
  };

  const updateText = () => {
    document.querySelectorAll('.native-text').forEach(makeSelectable);
  };

  const selectionStyle = document.createElement('style');
  selectionStyle.textContent = `
    .native-text::selection {
      background: #ccc;
      color: black;
    }
  `;
  document.head.appendChild(selectionStyle);

  updateText();

  new MutationObserver(updateText).observe(document.body, {
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
            if (mutation.addedNodes.length)
                applyOverlayStyle();
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

            if (window.isFeed()) scroller.style.paddingBottom = '0';

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
    new MutationObserver(applyStyles).observe(document.body, { childList: true, subtree: true });
})();

/* The below scripts are specific to com.ycngmn.Nobook application. */

// Nobook settings : floating button
(() => {
  const getFill = () => {
    const color = document.querySelector('meta[name="theme-color"]')?.content?.toLowerCase();
    return color === '#ffffff' ? '#65676b' : '#d0d0d0';
  };

  const btn = Object.assign(document.createElement('button'), {
    innerHTML: `
      <svg width="28" height="28" fill="${getFill()}" viewBox="0 0 24 24">
        <path fill-rule="evenodd" d="M9.586 2.586A2 2 0 0 1 11 2h2a2 2 0 0 1 2 2v.089l.473.196.063-.063a2.002 2.002 0 0 1 2.828 0l1.414 1.414a2 2 0 0 1 0 2.827l-.063.064.196.473H20a2 2 0 0 1 2 2v2a2 2 0 0 1-2 2h-.089l-.196.473.063.063a2.002 2.002 0 0 1 0 2.828l-1.414 1.414a2 2 0 0 1-2.828 0l-.063-.063-.473.196V20a2 2 0 0 1-2 2h-2a2 2 0 0 1-2-2v-.089l-.473-.196-.063.063a2.002 2.002 0 0 1-2.828 0l-1.414-1.414a2 2 0 0 1 0-2.827l.063-.064L4.089 15H4a2 2 0 0 1-2-2v-2a2 2 0 0 1 2-2h.09l.195-.473-.063-.063a2 2 0 0 1 0-2.828l1.414-1.414a2 2 0 0 1 2.827 0l.064.063L9 4.089V4a2 2 0 0 1 .586-1.414ZM8 12a4 4 0 1 1 8 0 4 4 0 0 1-8 0Z" clip-rule="evenodd"/>
      </svg>`,
    style: `
      position: fixed;
      top: 8px;
      right: 100px;
      background: transparent;
      border: none;
      border-radius: 50%;
      cursor: pointer;
      display: none;
      align-items: center;
      justify-content: center;
    `
  });

  btn.onclick = () => SettingsBridge?.onSettingsToggle?.();
  (document.body || document.addEventListener('DOMContentLoaded', () => document.body.appendChild(btn))) && document.body.appendChild(btn);

  new MutationObserver(() => {
    btn.style.display = window.isFeed() ? 'flex' : 'none';
  }).observe(document.body, { childList: true, subtree: true });

  new MutationObserver(() => {
    const svg = btn.querySelector('svg');
    if (svg) svg.setAttribute('fill', getFill());
  }).observe(document.head, { subtree: true, attributes: true, childList: true });

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
            if (reader.result)
                DownloadBridge.downloadBase64File(reader.result, blob.type);
        };
        reader.readAsDataURL(blob);
        return originalCreateObjectURL(blob);
    };
})();