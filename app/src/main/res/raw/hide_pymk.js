// hides people you may know container
(function() {
  if (window.location.href.includes('facebook.com/search/')) {
    const imageArea = document.querySelector('[data-mcomponent="ImageArea"]');
    if (imageArea && imageArea.nextElementSibling) {
      imageArea.nextElementSibling.style.display = 'none';
    }
  }
})();

