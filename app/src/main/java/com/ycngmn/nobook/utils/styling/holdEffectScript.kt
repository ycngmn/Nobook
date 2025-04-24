package com.ycngmn.nobook.utils.styling

// This script adds a grey overlay on elements when they are held down or clicked.
// while preventing any overlay when scrolling.
// Generated using : Claude and Grok
val holdEffectScript = """
  (function() {
    const style = document.createElement('style');
    style.innerHTML = `
        * { -webkit-tap-highlight-color: transparent !important; }
        .fb-hold-effect {
            position: relative;
        }
        .fb-hold-effect::after {
            content: '';
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(180, 180, 180, 0.35);
            pointer-events: none;
            z-index: 9999;
        }
    `;
    document.head.appendChild(style);

    let holdTimer, heldElement, touchStartY = 0, isScrolling = false;
    const HOLD_DELAY = 125, SCROLL_THRESHOLD = 5;

    const removeEffects = () => {
        clearTimeout(holdTimer);
        if (heldElement) {
            heldElement.classList.remove('fb-hold-effect');
            heldElement = null;
        }
    };

    document.addEventListener('touchstart', e => {
        touchStartY = e.touches[0].clientY;
        isScrolling = false;
        removeEffects();
        heldElement = e.target;
        if (heldElement.tagName.toLowerCase() !== 'body') {
            holdTimer = setTimeout(() => {
                if (!isScrolling) heldElement.classList.add('fb-hold-effect');
            }, HOLD_DELAY);
        }
    }, true);

    document.addEventListener('touchmove', e => {
        if (Math.abs(e.touches[0].clientY - touchStartY) > SCROLL_THRESHOLD) {
            isScrolling = true;
            removeEffects();
        }
    }, true);

    document.addEventListener('click', e => {
        if (isScrolling) return;
        const target = e.target;
        if (target.tagName.toLowerCase() !== 'body') {
            target.classList.add('fb-hold-effect');
            setTimeout(() => target.classList.remove('fb-hold-effect'), 100);
        }
    }, true);

    ['touchend', 'touchcancel'].forEach(event => 
        document.addEventListener(event, removeEffects, true)
    );

    new MutationObserver(() => {
        if (!document.head.contains(style)) document.head.appendChild(style);
    }).observe(document.body, { childList: true, subtree: true });
  })();
""".trimIndent()