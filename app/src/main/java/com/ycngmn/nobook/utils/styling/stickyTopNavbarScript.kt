package com.ycngmn.nobook.utils.styling

val stickyTopNavbarScript = """
(function() {
    const applyStyles = () => {
        const navbar = document.querySelector('div[data-tti-phase="-1"][data-mcomponent="MContainer"][data-type="container"][data-focusable="true"].m');
        const tabbar = document.querySelector('div[role="tablist"][data-tti-phase="-1"][data-type="container"][data-mcomponent="MContainer"].m');
        const scroller = document.querySelector('div[data-type="vscroller"][data-is-pull-to-refresh-allowed="true"]');

        const hasLogo = navbar?.querySelector('div[aria-label="Logo Facebook"]');
        const hasFeed = tabbar?.querySelector('div[aria-label*="feed"]');

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
            top: hasLogo ? '43px' : '',
            left: '0',
            width: '100%',
            zIndex: '999',
            pointerEvents: 'auto'
        });

        if (scroller) {
            const offset = (hasLogo ? 43 : 0) + (hasFeed ? 50 : 0);
            const scrollContent = scroller.querySelector(':scope > div:not(.pull-to-refresh-spinner-container)');
            scrollContent ? scrollContent.style.marginTop = offset + 'px' : scroller.style.paddingTop = offset + 'px';

            const spinnerContainer = scroller.querySelector('.pull-to-refresh-spinner-container');
            if (spinnerContainer) Object.assign(spinnerContainer.style, {
                position: 'absolute',
                top: '0',
                left: '0',
                right: '0',
                zIndex: '1001',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center'
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
""".trimIndent()
