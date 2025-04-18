package com.ycngmn.nobook.utils.styling

// context : https://stackoverflow.com/a/73960585/11724248
val fixWebViewVideoPosterScript = """
    (function() {
        const posterValue = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
        
        function setPosters() {
            const videos = document.querySelectorAll('video');
            videos.forEach(video => {
                if (!video.hasAttribute('poster')) {
                    video.setAttribute('poster', posterValue);
                }
            });
        }

        setPosters();

        // Watch for new video elements
        const observer = new MutationObserver(() => {
            setPosters();
        });

        observer.observe(document.body || document.documentElement, {
            childList: true,
            subtree: true
        });
    })();
""".trimIndent()