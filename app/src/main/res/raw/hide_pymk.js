// hides people you may know container
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


