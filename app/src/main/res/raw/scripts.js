// Desktop mode identifier
(() => {
    window.isDesktopMode = () => {
        return (
            document.querySelector(
                'html[id="facebook"]'
            ) !== null
        );
    };
})();


// Feed identifier
(() => {
    window.isFeed = () => {
        const isHomeUrl =
            window.location.pathname === "/" &&
            (
                window.location.hostname ===
                    "m.facebook.com" ||
                window.location.hostname ===
                    "www.facebook.com"
            );

        if (window.isDesktopMode()) {
            return isHomeUrl;
        }

        const hasSpecialButton =
            Array.from(
                document.querySelectorAll(
                    '[role="button"] span'
                )
            ).some(
                span =>
                    span.textContent === "󱥆"
            );

        return isHomeUrl && hasSpecialButton;
    };
})();


/*
 * Desktop-layout adjustments.
 */
(function () {
    if (!window.isDesktopMode()) {
        return;
    }

    document.documentElement.style.fontSize =
        "18px";

    /*
     * Do not stick the desktop Facebook navbar
     * unless the separate sticky-navbar feature
     * is enabled.
     */
    (() => {
        const waitForBanner =
            () =>
                new Promise(resolve => {
                    const existing =
                        document.querySelector(
                            'div[role="banner"]'
                        );

                    if (existing) {
                        resolve(existing);
                        return;
                    }

                    new MutationObserver(
                        (mutations, observer) => {
                            for (
                                const {
                                    addedNodes
                                } of mutations
                            ) {
                                for (
                                    const node of
                                    addedNodes
                                ) {
                                    if (
                                        node.nodeType ===
                                            Node.ELEMENT_NODE &&
                                        node.matches(
                                            'div[role="banner"]'
                                        )
                                    ) {
                                        observer.disconnect();
                                        resolve(node);
                                        return;
                                    }
                                }
                            }
                        }
                    ).observe(
                        document.body,
                        {
                            childList: true,
                            subtree: true
                        }
                    );
                });

        const forceAbsolute = element => {
            if (
                element?.classList.contains(
                    "xixxii4"
                )
            ) {
                element.style.setProperty(
                    "position",
                    "absolute",
                    "important"
                );
            }
        };

        waitForBanner().then(banner => {
            const style =
                document.createElement("style");

            style.textContent = `
                div[role="banner"].xixxii4,
                div[role="banner"] .xixxii4 {
                    position: absolute !important;
                }
            `;

            document.head.appendChild(style);

            forceAbsolute(banner);

            banner
                .querySelectorAll(".xixxii4")
                .forEach(forceAbsolute);

            new MutationObserver(mutations => {
                for (const mutation of mutations) {
                    if (
                        mutation.type ===
                            "childList"
                    ) {
                        mutation.addedNodes.forEach(
                            node => {
                                forceAbsolute(node);

                                node
                                    .querySelectorAll?.(
                                        ".xixxii4"
                                    )
                                    ?.forEach(
                                        forceAbsolute
                                    );
                            }
                        );
                    } else if (
                        mutation.type ===
                            "attributes" &&
                        mutation.attributeName ===
                            "class"
                    ) {
                        forceAbsolute(
                            mutation.target
                        );
                    }
                }
            }).observe(
                banner,
                {
                    childList: true,
                    subtree: true,
                    attributes: true,
                    attributeFilter: ["class"]
                }
            );
        });
    })();


    /*
     * Remove the desktop Send button to save space.
     */
    (() => {
        const parentSelector =
            ".xbmvrgn.x1diwwjn";

        const childSelector =
            ".x10b6aqq.x1yrsyyn.xs83m0k";

        const checkAndRemoveThird =
            parent => {
                const children =
                    parent.querySelectorAll(
                        childSelector
                    );

                if (children.length === 4) {
                    children[2].remove();
                }
            };

        document
            .querySelectorAll(parentSelector)
            .forEach(checkAndRemoveThird);

        const observer =
            new MutationObserver(mutations => {
                for (
                    const mutation of mutations
                ) {
                    mutation.addedNodes.forEach(
                        node => {
                            if (
                                node.nodeType !==
                                Node.ELEMENT_NODE
                            ) {
                                return;
                            }

                            if (
                                node.matches(
                                    parentSelector
                                )
                            ) {
                                checkAndRemoveThird(
                                    node
                                );
                            }

                            node
                                .querySelectorAll(
                                    parentSelector
                                )
                                .forEach(
                                    checkAndRemoveThird
                                );
                        }
                    );
                }
            });

        observer.observe(
            document.body,
            {
                childList: true,
                subtree: true
            }
        );
    })();
})();


// Scroll to the top when Back is pressed at the feed.
(() => {
    window.backHandlerNB = () => {
        const dialogs =
            document.querySelectorAll(
                'div[role="dialog"]'
            );

        const isMenu =
            document.querySelector(
                'div[role="menu"]'
            );

        const scrollToTop = () => {
            if (window.scrollY !== 0) {
                document.body.style.overflow =
                    "hidden";

                setTimeout(() => {
                    document.body.style.overflow =
                        "";

                    window.scrollTo({
                        top: 0,
                        behavior: "smooth"
                    });
                }, 30);

                return "scrolling";
            }

            return "exit";
        };

        if (window.isDesktopMode()) {
            if (
                window.isFeed() &&
                !isMenu &&
                dialogs.length === 1
            ) {
                return scrollToTop();
            }

            if (
                isMenu ||
                dialogs.length > 1
            ) {
                const escapeEvent =
                    new KeyboardEvent(
                        "keydown",
                        {
                            key: "Escape",
                            code: "Escape",
                            keyCode: 27,
                            which: 27,
                            bubbles: true,
                            cancelable: true
                        }
                    );

                window.dispatchEvent(
                    escapeEvent
                );

                return "true";
            }

            return "false";
        }

        if (
            window.isFeed() &&
            !isMenu &&
            dialogs.length === 0
        ) {
            return scrollToTop();
        }

        return "false";
    };
})();


/*
 * Control the native floating bottom bar.
 *
 * Behaviour:
 * - hide while scrolling down
 * - show while scrolling up
 * - show at the top of the page
 * - hide in stories, reels and photo/video viewers
 * - hide during real browser fullscreen
 */
(() => {
    if (
        window
            .__nobookBottomBarControllerInstalled
    ) {
        if (
            window.NobookBottomBarController &&
            window.NobookBottomBarController
                .refresh
        ) {
            window.NobookBottomBarController
                .refresh();
        }

        return;
    }

    window
        .__nobookBottomBarControllerInstalled =
        true;

    const MIN_SCROLL_DELTA = 8;
    const TOP_REVEAL_DISTANCE = 24;

    let desiredVisible = true;
    let mediaViewerOpen = false;
    let lastReportedVisibility = null;
    let framePending = false;
    let mutationTimer = null;

    const scrollPositions = new WeakMap();

    const getRootScroller = () => {
        return (
            document.scrollingElement ||
            document.documentElement
        );
    };

    const normalizeScrollTarget = target => {
        if (
            target === document ||
            target === window ||
            target === document.body ||
            target === document.documentElement
        ) {
            return getRootScroller();
        }

        if (target instanceof Element) {
            return target;
        }

        return getRootScroller();
    };

    const isRelevantScroller = element => {
        if (!element) {
            return false;
        }

        if (element === getRootScroller()) {
            return true;
        }

        return (
            element.clientHeight >=
                window.innerHeight * 0.55 &&
            element.scrollHeight >
                element.clientHeight + 80
        );
    };

    const getScrollPosition = element => {
        if (element === getRootScroller()) {
            return Math.max(
                window.scrollY || 0,
                element.scrollTop || 0
            );
        }

        return element.scrollTop || 0;
    };

    const isVisible = element => {
        if (!element) {
            return false;
        }

        const style =
            window.getComputedStyle(element);

        if (
            style.display === "none" ||
            style.visibility === "hidden" ||
            Number(style.opacity) === 0
        ) {
            return false;
        }

        const rect =
            element.getBoundingClientRect();

        return (
            rect.width > 0 &&
            rect.height > 0 &&
            rect.bottom > 0 &&
            rect.right > 0 &&
            rect.top <
                window.innerHeight &&
            rect.left <
                window.innerWidth
        );
    };

    const isMediaRoute = () => {
        const path =
            window.location.pathname
                .toLowerCase();

        return (
            path.includes("/reel/") ||
            path.includes("/reels/") ||
            path.includes("/stories/") ||
            path.includes("/watch/") ||
            path === "/watch" ||
            path.includes("/videos/") ||
            path.includes("/photo/") ||
            path.includes("/photos/") ||
            path.includes("/share/r/")
        );
    };

    const isBrowserFullscreen = () => {
        if (
            document.fullscreenElement ||
            document.webkitFullscreenElement
        ) {
            return true;
        }

        return Array.from(
            document.querySelectorAll("video")
        ).some(
            video =>
                video.webkitDisplayingFullscreen ===
                true
        );
    };

    const hasLargeMediaOverlay = () => {
        const selectors = [
            'div[role="dialog"]',
            'div[data-pagelet="Story"]',
            'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"]'
        ];

        const containers =
            selectors.flatMap(selector =>
                Array.from(
                    document.querySelectorAll(
                        selector
                    )
                )
            );

        return containers.some(container => {
            if (!isVisible(container)) {
                return false;
            }

            const containerRect =
                container.getBoundingClientRect();

            const isLargeContainer =
                containerRect.width >=
                    window.innerWidth * 0.75 &&
                containerRect.height >=
                    window.innerHeight * 0.5;

            if (!isLargeContainer) {
                return false;
            }

            const mediaElements =
                container.querySelectorAll(
                    [
                        "video",
                        'img[src*="fbcdn"]',
                        'img[src*="scontent"]'
                    ].join(",")
                );

            return Array.from(
                mediaElements
            ).some(media => {
                if (!isVisible(media)) {
                    return false;
                }

                const rect =
                    media.getBoundingClientRect();

                return (
                    rect.width >= 180 &&
                    rect.height >= 180
                );
            });
        });
    };

    const detectMediaViewer = () => {
        return (
            isBrowserFullscreen() ||
            isMediaRoute() ||
            hasLargeMediaOverlay()
        );
    };

    const reportVisibility = visible => {
        if (
            lastReportedVisibility === visible
        ) {
            return;
        }

        lastReportedVisibility = visible;

        try {
            if (
                window.BrowserUiBridge &&
                window.BrowserUiBridge
                    .setBottomBarVisible
            ) {
                window.BrowserUiBridge
                    .setBottomBarVisible(
                        visible
                    );
            }
        } catch (error) {
            console.debug(
                "Unable to update Nobook bar",
                error
            );
        }
    };

    const applyVisibility = () => {
        framePending = false;

        if (mediaViewerOpen) {
            reportVisibility(false);
            return;
        }

        const rootPosition =
            getScrollPosition(
                getRootScroller()
            );

        if (
            rootPosition <=
            TOP_REVEAL_DISTANCE
        ) {
            desiredVisible = true;
        }

        reportVisibility(
            desiredVisible
        );
    };

    const scheduleVisibilityUpdate = () => {
        if (framePending) {
            return;
        }

        framePending = true;

        window.requestAnimationFrame(
            applyVisibility
        );
    };

    const refreshMediaState = () => {
        const nextMediaState =
            detectMediaViewer();

        if (
            mediaViewerOpen &&
            !nextMediaState
        ) {
            /*
             * Show the controls again after
             * closing a reel/photo/video viewer.
             */
            desiredVisible = true;
        }

        mediaViewerOpen =
            nextMediaState;

        scheduleVisibilityUpdate();
    };

    const onScroll = event => {
        const target =
            normalizeScrollTarget(
                event.target
            );

        if (
            !isRelevantScroller(target)
        ) {
            return;
        }

        const currentPosition =
            getScrollPosition(target);

        const previousPosition =
            scrollPositions.has(target)
                ? scrollPositions.get(target)
                : currentPosition;

        const delta =
            currentPosition -
            previousPosition;

        scrollPositions.set(
            target,
            currentPosition
        );

        if (
            currentPosition <=
            TOP_REVEAL_DISTANCE
        ) {
            desiredVisible = true;
        } else if (
            delta > MIN_SCROLL_DELTA
        ) {
            desiredVisible = false;
        } else if (
            delta < -MIN_SCROLL_DELTA
        ) {
            desiredVisible = true;
        }

        scheduleVisibilityUpdate();
    };

    const dispatchLocationChange = () => {
        window.dispatchEvent(
            new Event(
                "nobook-location-change"
            )
        );
    };

    ["pushState", "replaceState"]
        .forEach(methodName => {
            const originalMethod =
                history[methodName];

            history[methodName] =
                function (...args) {
                    const result =
                        originalMethod.apply(
                            this,
                            args
                        );

                    dispatchLocationChange();

                    return result;
                };
        });

    document.addEventListener(
        "scroll",
        onScroll,
        true
    );

    document.addEventListener(
        "fullscreenchange",
        refreshMediaState
    );

    document.addEventListener(
        "webkitfullscreenchange",
        refreshMediaState
    );

    window.addEventListener(
        "popstate",
        refreshMediaState
    );

    window.addEventListener(
        "hashchange",
        refreshMediaState
    );

    window.addEventListener(
        "nobook-location-change",
        refreshMediaState
    );

    window.addEventListener(
        "resize",
        refreshMediaState
    );

    window.addEventListener(
        "orientationchange",
        refreshMediaState
    );

    document.addEventListener(
        "visibilitychange",
        refreshMediaState
    );

    /*
     * Facebook adds/removes viewer elements
     * dynamically, so refresh after DOM changes.
     * The timeout prevents excessive processing.
     */
    const observer =
        new MutationObserver(() => {
            window.clearTimeout(
                mutationTimer
            );

            mutationTimer =
                window.setTimeout(
                    refreshMediaState,
                    100
                );
        });

    observer.observe(
        document.body,
        {
            childList: true,
            subtree: true
        }
    );

    window.NobookBottomBarController =
        Object.freeze({
            refresh:
                refreshMediaState
        });

    refreshMediaState();
})();


// Enable press-and-hold caption selection.
(() => {
    const makeSelectable = element => {
        if (
            element.closest(
                'div[role="button"]'
            )
        ) {
            return;
        }

        element.style.userSelect = "text";
        element.style.pointerEvents = "auto";
    };

    const updateText = () => {
        document
            .querySelectorAll(
                ".native-text"
            )
            .forEach(makeSelectable);
    };

    const selectionStyle =
        document.createElement("style");

    selectionStyle.textContent = `
        .native-text::selection {
            background: #ccc;
            color: black;
        }
    `;

    document.head.appendChild(
        selectionStyle
    );

    updateText();

    new MutationObserver(
        updateText
    ).observe(
        document.body,
        {
            childList: true,
            subtree: true
        }
    );
})();


// Improve Facebook loading overlays.
(() => {
    const applyOverlayStyle = () => {
        document
            .querySelectorAll(
                ".loading-overlay"
            )
            .forEach(overlay => {
                overlay.style
                    .backgroundColor =
                    "rgba(0, 0, 0, 0.1)";
            });
    };

    applyOverlayStyle();

    new MutationObserver(
        mutations => {
            if (
                mutations.some(
                    mutation =>
                        mutation.addedNodes
                            .length > 0
                )
            ) {
                applyOverlayStyle();
            }
        }
    ).observe(
        document.body,
        {
            childList: true,
            subtree: true
        }
    );
})();


// Hide Facebook's login-page download banner.
(() => {
    const element =
        document.querySelector(
            'div[data-bloks-name="bk.components.Flexbox"].wbloks_1'
        );

    if (element) {
        element.remove();
    }
})();


// Hide small unwanted bottom banners.
(() => {
    const observer =
        new MutationObserver(() => {
            if (
                location.pathname === "/" &&
                document.querySelector(
                    'div[role="button"][aria-label*="Facebook"]'
                ) === null
            ) {
                return;
            }

            const element =
                document.querySelector(
                    ".bottom.fixed-container"
                );

            if (
                !element ||
                element.hasAttribute(
                    "data-shift-on-keyboard-shown"
                )
            ) {
                return;
            }

            const heightAttribute =
                element.getAttribute(
                    "data-actual-height"
                );

            if (
                heightAttribute &&
                Number.parseInt(
                    heightAttribute,
                    10
                ) < 80
            ) {
                element.style.display =
                    "none";
            }
        });

    observer.observe(
        document.body,
        {
            childList: true,
            subtree: true
        }
    );
})();


// Android-like touch highlight.
(() => {
    const style =
        document.createElement("style");

    style.textContent = `
        * {
            -webkit-tap-highlight-color:
                rgba(180, 180, 180, 0.35);
        }
    `;

    document.head.appendChild(style);
})();


/*
 * Notify Android when Facebook changes its
 * page theme colour.
 */
(() => {
    const meta =
        document.querySelector(
            'meta[name="theme-color"]'
        );

    const notify = () => {
        if (
            window.ThemeBridge &&
            window.ThemeBridge
                .onThemeColorChanged
        ) {
            window.ThemeBridge
                .onThemeColorChanged(
                    meta?.content ?? "null"
                );
        }
    };

    if (!meta) {
        return;
    }

    notify();

    new MutationObserver(
        notify
    ).observe(
        meta,
        {
            attributes: true,
            attributeFilter: ["content"]
        }
    );
})();


/*
 * Preserve the original file-download bridge.
 */
(() => {
    if (
        window
            ._downloadBridgeInitialized
    ) {
        return;
    }

    window
        ._downloadBridgeInitialized =
        true;

    const originalCreateObjectURL =
        URL.createObjectURL;

    URL.createObjectURL =
        function (blob) {
            const reader =
                new FileReader();

            reader.onloadend =
                function () {
                    if (
                        reader.result &&
                        window.DownloadBridge &&
                        window.DownloadBridge
                            .downloadBase64File
                    ) {
                        window.DownloadBridge
                            .downloadBase64File(
                                reader.result,
                                blob.type
                            );
                    }
                };

            reader.readAsDataURL(blob);

            return originalCreateObjectURL(
                blob
            );
        };
})();