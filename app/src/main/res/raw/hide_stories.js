// Hide "story" container from feed.
(function() {
  const hideTargetElement = () => {
    const el = document.querySelector('[data-mcomponent="MContainer"][data-srat="43"]');
    if (el) {
      el.style.display = 'none';
    }
  };

  hideTargetElement();

  const observer = new MutationObserver(() => {
    hideTargetElement();
  });

  observer.observe(document.body, {
    childList: true,
    subtree: true
  });
})();
