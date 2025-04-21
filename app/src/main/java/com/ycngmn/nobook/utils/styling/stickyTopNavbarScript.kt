package com.ycngmn.nobook.utils.styling

val stickyTopNavbarScript =
    """
    (function() {
        var a = function() {
            var h = document.querySelector('div[data-tti-phase="-1"][data-mcomponent="MContainer"][data-type="container"][data-focusable="true"].m');
            if (h && h.querySelector('div[aria-label="Logo Facebook"]')) {
                h.style.position = 'fixed';
                h.style.top = '0';
                h.style.left = '0';
                h.style.width = '100%';
                h.style.zIndex = '1000';
                h.style.pointerEvents = 'auto';
            }
    
            var t = document.querySelector('div[role="tablist"][data-tti-phase="-1"][data-type="container"][data-mcomponent="MContainer"].m');
            if (t && t.querySelector('div[aria-label*="feed"]')) {
                t.style.position = 'fixed';
                t.style.top = (h && h.querySelector('div[aria-label="Logo Facebook"]')) ? '43px' : '';
                t.style.left = '0';
                t.style.width = '100%';
                t.style.zIndex = '999';
                t.style.pointerEvents = 'auto';
            }
    
            var v = document.querySelector('div[data-type="vscroller"][data-is-pull-to-refresh-allowed="true"]');
            if (v) {
                var o = (h ? 43 : 0) + (t ? 50 : 0);
                var c = v.querySelector(':scope > div:not(.pull-to-refresh-spinner-container)');
                if (c) {
                    c.style.marginTop = o + 'px';
                } else {
                    v.style.paddingTop = o + 'px';
                }
    
                var s = v.querySelector('.pull-to-refresh-spinner-container');
                if (s) {
                    s.style.position = 'absolute';
                    s.style.top = '0';
                    s.style.left = '0';
                    s.style.right = '0';
                    s.style.zIndex = '1001';
                    s.style.display = 'flex';
                    s.style.justifyContent = 'center';
                    s.style.alignItems = 'center';
                }
    
                var p = v.querySelector('.pull-to-refresh-spinner');
                if (p) {
                    p.style.margin = '0 auto';
                }
            }
    
            document.body.style.paddingTop = '0';
            document.body.style.marginTop = '0';
            document.body.style.overflow = 'visible';
            document.body.style.height = '100%';
        };
    
        a();
        setTimeout(a, 2000);
        var o = new MutationObserver(a);
        o.observe(document.body, { childList: true, subtree: true });
    })();
    """.trimIndent()