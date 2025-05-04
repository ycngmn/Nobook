// hides people you may know container on search page
(function() {
  const hideSuggestedPeople = () => {
  if (window.location.href.includes('facebook.com/search/')) {
        const imageArea = document.querySelector('[data-mcomponent="ImageArea"]');
        if (imageArea && imageArea.nextElementSibling) {
          imageArea.nextElementSibling.style.display = 'none';
        }
    }
  };
  hideSuggestedPeople();

  const observer = new MutationObserver(() => {
    hideSuggestedPeople();
  });

  observer.observe(document.body, {
    childList: true,
    subtree: true
  });
})();

// Hides people you may know on feed
(() => {
  const hideParents = (elements) => {
    elements.forEach(el => {
      const parent = el.closest('[data-tracking-duration-id]');
      if (parent) parent.style.display = 'none';
    });
  };

  hideParents(document.querySelectorAll('.hscroller'));

  const observer = new MutationObserver(mutations => {
    mutations.forEach(({ addedNodes }) => {
      addedNodes.forEach(node => {
        if (!(node instanceof HTMLElement)) return;
        if (node.classList?.contains('hscroller')) {
          const parent = node.closest('[data-tracking-duration-id]');
          if (parent) parent.style.display = 'none';
        } else {
          node.querySelectorAll?.('.hscroller').forEach(el => {
            const parent = el.closest('[data-tracking-duration-id]');
            if (parent) parent.style.display = 'none';
          });
        }
      });
    });
  });

  observer.observe(document.body, { childList: true, subtree: true });
})();


