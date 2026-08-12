(function () {
  try {
    var css = `
      :root { --fb-blue: __ACCENT_PRIMARY__ !important; }
      body, html { background-color: __SURFACE__ !important; }
      [style*="rgb(24, 119, 242)"], [style*="#1877f2"] {
        color: __ACCENT_PRIMARY__ !important;
      }
    `;
    var tag = document.getElementById("nobook-material-you");
    if (!tag) {
      tag = document.createElement("style");
      tag.id = "nobook-material-you";
      (document.head || document.documentElement).appendChild(tag);
    }
    tag.textContent = css;
    console.info("[Nobook] Material You palette applied");
  } catch (err) {
    console.error("[Nobook] material_you.js injection failed:", err);
  }
})();
