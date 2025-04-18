package com.ycngmn.nobook.utils.styling

// On facebook mobile, there is a sticky banner at the bottom of the screen that asks you to open its app.
val hideOpenWithAppBannerScript = """
    (function() {
        const style = document.createElement('style');
        style.type = 'text/css';
        style.innerHTML = `
            div.fixed-container.bottom {
                display: none !important;
                visibility: hidden !important;
                height: 0 !important;
            }
        `;
        document.head.appendChild(style);
    })();
""".trimIndent()