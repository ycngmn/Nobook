// Hide suggested posts from feed
(function() {

    // Hide suggested posts from feed
    if (window.isDesktopMode()) {
      (() => {
        const containerSelector = '.x1yztbdb.x1n2onr6.xh8yej3.x1ja2u2z';
        const targetSpanSelector = 'span.xzsf02u.xo1l8bm.xdwrcjd';

        const hideMatchingContainers = () => {
          const containers = document.querySelectorAll(containerSelector);
          containers.forEach(container => {
            const targetSpan = container.querySelector(targetSpanSelector);
            if (targetSpan) {
              container.style.display = 'none';
            }
          });
        };

        hideMatchingContainers();

        const observer = new MutationObserver(() => {
          hideMatchingContainers();
        });

        observer.observe(document.body, {
          childList: true,
          subtree: true,
        });
      })();
    } else {
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
    }
})();

