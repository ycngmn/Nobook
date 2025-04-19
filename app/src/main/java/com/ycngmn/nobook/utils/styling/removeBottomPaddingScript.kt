package com.ycngmn.nobook.utils.styling

const val removeBottomPaddingScript =  """
    javascript:(function() {
        function removePaddings() {
            const el = document.querySelector('[data-pull-to-refresh-action-id]');
            if (el) {
                el.style.setProperty('padding-bottom', '0', 'important');
            }
        }

        removePaddings();

        new MutationObserver(mutations => {
            mutations.forEach(mutation => {
                mutation.addedNodes.forEach(node => {
                    if (node.nodeType === 1) {
                        if (node.hasAttribute('data-pull-to-refresh-action-id')) {
                            node.style.setProperty('padding-bottom', '0', 'important');
                        } else {
                            node.querySelectorAll?.('[data-pull-to-refresh-action-id]')
                                .forEach(el => el.style.setProperty('padding-bottom', '0', 'important'));
                        }
                    }
                });
            });
        }).observe(document.body, {
            childList: true,
            subtree: true
        });
    })();"""