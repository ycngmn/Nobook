// System font by default
(function() {

  function containsSpecialChars(str) {
    const specialChars = ['󰞋', '󱟡', '󱢏', '󱘋'];
    return specialChars.some(char => str.includes(char));
  }

  function processDiv(div) {

    div.childNodes.forEach(node => {

    // Wrap direct text nodes in a span with the desired font
    if (node.textContent.split(" ")[0].length === 2) return;
    if (containsSpecialChars(node.textContent)) return;

    const span = document.createElement('span');
    span.style.fontFamily = 'system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", sans-serif';
    span.textContent = node.textContent;
    div.replaceChild(span, node);

    });
  }

  // Process initial divs
  const divs = document.querySelectorAll('div.native-text, div.native-text span');
  divs.forEach(processDiv);

  const observer = new MutationObserver(mutations => {
    mutations.forEach(mutation => {
      mutation.addedNodes.forEach(node => {
        if (node.nodeType === Node.ELEMENT_NODE) {
          // Check if the added node itself is a div.native-text


          // Check for div.native-text elements within the added node
          const newDivs = node.querySelectorAll ? node.querySelectorAll('div.native-text, div.native-text span') : [];
          newDivs.forEach(processDiv);
        }
      });
    });
  });

  observer.observe(document.body, {
    childList: true,
    subtree: true
  });
})();