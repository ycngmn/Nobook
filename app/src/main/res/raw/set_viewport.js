// For messenger webview.
// It breaks below 376px width. So enforced the minimum.

(function () {
  if (location.hostname === "www.messenger.com") {
    const meta = document.querySelector('meta[name="viewport"]');
    if (meta) {
      meta.setAttribute("content", "width=376, user-scalable=no");
    } else {
      const newMeta = document.createElement("meta");
      newMeta.name = "viewport";
      newMeta.content = "width=376, user-scalable=no";
      document.head.appendChild(newMeta);
    }
  }
})();