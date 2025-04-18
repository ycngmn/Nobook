package com.ycngmn.nobook.utils.styling

// Add more transparency to the default loading overlay.
val enhanceLoadingOverlayScript = """
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
""".trimIndent()