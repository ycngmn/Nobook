// Hide suggested posts from feed
(function() {
    // Apply only in Facebook feed
    const feedUrlPattern = /^https:\/\/(m\.facebook\.com\/|www\.facebook\.com\/?)$/;
    if (!feedUrlPattern.test(window.location.href)) return;

    function hideSuggestedPosts() {
        const postTextContainers = document.querySelectorAll(
            'div[data-mcomponent="TextArea"][data-type="text"][data-tti-phase="-1"] .native-text > span'
        );

        postTextContainers.forEach(textSpan => {
            if (textSpan.textContent.trim() === '•') {
                const suggestedPostContainer = textSpan.closest('div[data-tracking-duration-id]');
                if (suggestedPostContainer) {
                    // Hide the suggested post
                    suggestedPostContainer.style.display = 'none';

                    // Hide the previous sibling (gap between posts)
                    const postGap = suggestedPostContainer.previousElementSibling;
                    if (postGap) {
                        postGap.style.display = 'none';
                    }
                }
            }
        });
    }
    hideSuggestedPosts();
    const feedObserver = new MutationObserver(hideSuggestedPosts);
    feedObserver.observe(document.body, { childList: true, subtree: true });
})();
