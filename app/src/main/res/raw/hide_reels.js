(() => {
  const REEL_ICON = '󱣝';
  const PROCESSED_ATTR = 'data-reel-hidden';

  function processNode(node) {
    if (!(node instanceof HTMLElement)) return;

    const candidates = node.matches('.fl.ac .native-text')
      ? [node]
      : node.querySelectorAll('.fl.ac .native-text');

    candidates.forEach(el => {
      if (el.textContent.trim() === REEL_ICON) {
        const container = el.closest('[data-tracking-duration-id]');
        if (container && !container.hasAttribute(PROCESSED_ATTR)) {
          container.setAttribute(PROCESSED_ATTR, '1');
          container.style.display = 'none';
        }
      }
    });
  }

  document.querySelectorAll('.fl.ac .native-text').forEach(processNode);

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
})();
