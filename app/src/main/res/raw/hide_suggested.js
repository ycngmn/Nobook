// Hide suggested posts from feed
(function() {

    function processNode(node) {
        if (!(node instanceof HTMLElement)) return;
        if (window.location.pathname !== '/') return;

        const textSpans = node.querySelectorAll(
            'div[data-mcomponent="TextArea"][data-type="text"][data-tti-phase="-1"] .native-text > span'
        );

        textSpans.forEach(textSpan => {
            if (textSpan.textContent.trim() === '•') {
                const suggestedPostContainer = textSpan.closest('div[data-tracking-duration-id]');
                if (suggestedPostContainer) {
                    suggestedPostContainer.style.display = 'none';
                    const postGap = suggestedPostContainer.previousElementSibling;
                    if (postGap) {
                        postGap.style.display = 'none';
                    }
                }
            }
        });
    }

    const observer = new MutationObserver(mutations => {
        for (const mutation of mutations) {
            for (const node of mutation.addedNodes) {
                processNode(node);
            }
        }
    });

    observer.observe(document.body, {
        childList: true,
        subtree: true
    });


    processNode(document.body);
})();

