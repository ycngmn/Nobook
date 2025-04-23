package com.ycngmn.nobook.utils.styling

// On facebook mobile, there is a sticky banner at the bottom of the screen.
const val HIDE_OPEN_WITH_APP_BANNER_SCRIPT = """
        (function() {
            const style = document.createElement('style');
            style.innerHTML = 'div.fixed-container.bottom { display: none !important; }';
            document.head.appendChild(style);
        })();
        """