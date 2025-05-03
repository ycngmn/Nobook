(() => {
  const targetIcon = '󱣝'; // fb reel icon.

  function processNode(node) {
    if (!(node instanceof Element)) return;

    const candidates = node.matches('.fl.ac .native-text') ? [node] :
      node.querySelectorAll('.fl.ac .native-text');

    candidates.forEach(el => {
      if (el.textContent.trim() === targetIcon) {
        const parent = el.closest('[data-tracking-duration-id]');
        if (parent) {
          parent.style.display = 'none';

          // Remove top paddings
          let prev = parent.previousElementSibling;
          while (prev && prev.getAttribute('data-actual-height') === '1') {
            const toRemove = prev;
            prev = prev.previousElementSibling;
            toRemove.remove();
          }

          // Remove bottom paddings
          let next = parent.nextElementSibling;
          while (next && next.getAttribute('data-actual-height') === '1') {
            const toRemove = next;
            next = next.nextElementSibling;
            toRemove.remove();
          }
        }
      }
    });
  }

  document.querySelectorAll('.fl.ac .native-text').forEach(processNode);

  const observer = new MutationObserver(mutations => {
    mutations.forEach(mutation => {
      mutation.addedNodes.forEach(processNode);
    });
  });

  observer.observe(document.body, {
    childList: true,
    subtree: true
  });
})();
