package com.ycngmn.nobook.utils.styling

val removeNavbarScript =
"""
        (function() {
            var hideDivs = function() {
                // Hide the header div (containing Logo Facebook)
                var headerDiv = document.querySelector('div[data-tti-phase="-1"][data-mcomponent="MContainer"][data-type="container"][data-focusable="true"].m');
                if (headerDiv && headerDiv.querySelector('div[aria-label="Logo Facebook"]')) {
                    headerDiv.style.display = 'none';
                }
                // Hide the tablist div (containing feed tab)
                var tablistDiv = document.querySelector('div[role="tablist"][data-tti-phase="-1"][data-type="container"][data-mcomponent="MContainer"].m');
                if (tablistDiv && tablistDiv.querySelector('div[aria-label*="feed"]')) {
                    tablistDiv.style.display = 'none';
                }
            };
            hideDivs();
            var observer = new MutationObserver(hideDivs);
            observer.observe(document.body, { childList: true, subtree: true });
        })();
    """.trimIndent()