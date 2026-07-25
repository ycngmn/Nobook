(function () {
    if (window.__nobookStickyNavbarInstalled) {
        return;
    }

    window.__nobookStickyNavbarInstalled = true;

    if (window.isDesktopMode()) {
        return;
    }

    const NAVBAR_SELECTOR =
        'div[data-tti-phase="-1"]' +
        '[data-mcomponent="MContainer"]' +
        '[data-type="container"]' +
        '[data-focusable="true"].m';

    const TABBAR_SELECTOR =
        'div[role="tablist"]' +
        '[data-tti-phase="-1"]' +
        '[data-type="container"]' +
        '[data-mcomponent="MContainer"].m';

    let scheduled = false;

    function applyStickyNavigation() {
        scheduled = false;

        const navbar = document.querySelector(NAVBAR_SELECTOR);
        const tabbar = document.querySelector(TABBAR_SELECTOR);

        const bars = [navbar, tabbar]
            .filter(Boolean)
            .filter((element, index, array) => {
                return array.indexOf(element) === index;
            });

        if (bars.length === 0) {
            return;
        }

        /*
         * Use the elements' original visual positions instead of assuming
         * that Facebook always places the logo row before the tab row.
         */
        bars.sort((first, second) => {
            return (
                first.getBoundingClientRect().top -
                second.getBoundingClientRect().top
            );
        });

        let topOffset = 0;

        bars.forEach((bar, index) => {
            const height = bar.getBoundingClientRect().height;

            bar.style.setProperty("position", "sticky", "important");
            bar.style.setProperty("top", `${topOffset}px`, "important");
            bar.style.setProperty("left", "0", "important");
            bar.style.setProperty("width", "100%", "important");
            bar.style.setProperty(
                "z-index",
                `${1000 - index}`,
                "important"
            );

            topOffset += height;
        });
    }

    function scheduleUpdate() {
        if (scheduled) {
            return;
        }

        scheduled = true;
        requestAnimationFrame(applyStickyNavigation);
    }

    applyStickyNavigation();

    const observer = new MutationObserver(scheduleUpdate);

    observer.observe(document.body, {
        childList: true,
        subtree: true,
    });
})();