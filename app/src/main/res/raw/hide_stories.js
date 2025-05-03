// Hide "story" container from feed.
(function() {
  const el = document.querySelector('[data-mcomponent="MContainer"][data-srat="43"]');
  if (el) {
    el.remove();
  }
})();