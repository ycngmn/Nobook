package com.ycngmn.nobook.utils

val zoomDisableScript = """
    (function() {
        function applyViewportLock() {
            let viewport = document.querySelector('meta[name="viewport"]');
            if (!viewport) {
                viewport = document.createElement('meta');
                viewport.name = "viewport";
                document.head.appendChild(viewport);
            }
            viewport.content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no";
        }

        // Initial call
        applyViewportLock();

        // Reapply on mutations (e.g., navigation, SPA behavior)
        const observer = new MutationObserver(() => {
            applyViewportLock();
        });

        observer.observe(document.head || document.documentElement, {
            childList: true,
            subtree: true
        });
    })();
""".trimIndent()