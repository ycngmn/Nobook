/*
 * Script to add copy to clipboard buttons for images on Facebook
 * Based on download_content.js
 */

(function() {
  // Configuration
  const CONFIG = {
    buttonZIndex: 999999,
    debug: false
  };

  // Global state
  let isProcessing = false;
  let currentContentContainer = null;
  let lastCopiedUrl = null;
  const COPY_BTN_ID = "nobook-clipboard-copier";

  // Selectors for finding media content
  const SELECTORS = {
    mediaElements: [
      'div[role="dialog"] img[src*="fbcdn"]:not([width="16"]):not([hidden])',
      'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"] img[src*="fbcdn"]',
      'div[data-pagelet="Story"] img[src*="fbcdn"]',
      'div[aria-label*="reel"] img[src*="fbcdn"]',
      'div[data-pagelet="ProfilePhoto"] img[src*="fbcdn"]'
    ],
    containers: [
      'div[role="dialog"]',
      'div[data-pagelet="Story"]',
      'div[aria-label*="story"]',
      '.story-viewer',
      '.story_viewer',
      'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"]',
      'div[data-pagelet="ProfilePhoto"]',
      'div[aria-label*="photo"]',
      'div[data-pagelet*="ProfileAppSection"]'
    ],
    contentIndicators: [
      'div[data-sigil="story-viewer"]',
      'div[data-sigil="story-popup-header"]',
      'div[data-sigil="story-tray-item"]',
      ".story_body_container",
      ".story_viewer",
      ".story-container",
      'div[aria-label*="highlight"]',
      'div[aria-label*="Highlight"]',
      'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"]',
      'div[data-pagelet="ProfilePhoto"]'
    ]
  };

  // Utility functions
  const debugLog = (...args) => CONFIG.debug && console.log("[ClipboardCopier]", ...args);

  const isElementVisible = (element) => {
    const rect = element.getBoundingClientRect();
    return (
      rect.top >= 0 &&
      rect.left >= 0 &&
      rect.bottom <= (window.innerHeight || document.documentElement.clientHeight)
    );
  };


  // Find the appropriate container for the content
  const findContentContainer = (element) => {
    if (!element) return null;

    for (const selector of SELECTORS.containers) {
      const container = element.closest(selector);
      if (container) return container;
    }

    return element.parentElement;
  };

  // Get the current visible image element
  const getCurrentImageElement = () => {
    // Try each selector in order of priority
    for (const selector of SELECTORS.mediaElements) {
      const elements = document.querySelectorAll(selector);

      // Find the first visible element
      for (const element of elements) {
        if (isElementVisible(element) && element.src) {
          return element;
        }
      }
    }

    // Fallback: look for any large visible image
    return Array.from(
      document.querySelectorAll('img[src*="fbcdn"]:not([width="16"]):not([hidden])')
    ).find(el => {
      const rect = el.getBoundingClientRect();
      return isElementVisible(el) && rect.width > 150 && rect.height > 150 && el.src;
    });
  };

  // Check if we are in a story or reel view
  const isInContentView = () => {
    // URL pattern checks
    const url = window.location.href;
    if (
      url.includes("/stories/") ||
      url.includes("/videos/") ||
      url.includes("/watch/?") ||
      url.includes("/photo") ||
      url.includes("/photos/") ||
      url.includes("/highlights/")
    ) {
      return true;
    }

    // Element selectors check
    for (const selector of SELECTORS.contentIndicators) {
      if (document.querySelector(selector)) {
        return true;
      }
    }

    return false;
  };

  // Copy image to clipboard
  const copyImageToClipboard = (url) => {
    fetch(url)
      .then(response => response.blob())
      .then(blob => {
        if (window.ClipboardBridge && window.ClipboardBridge.copyImageToClipboard) {
          const reader = new FileReader();
          reader.onloadend = function() {
            if (reader.result) {
              window.ClipboardBridge.copyImageToClipboard(
                reader.result,
                blob.type || "image/jpeg"
              );
            }
          };
          reader.readAsDataURL(blob);
        } else {
          try {
            navigator.clipboard.write([
              new ClipboardItem({
                [blob.type]: blob
              })
            ]).then(() => {
              // Success - bridge will show toast
            }).catch(err => {
              console.error("Clipboard API error:", err);
            });
          } catch (err) {
            console.error("Clipboard not supported:", err);
          }
        }
      })
      .catch(err => {
        console.error("Error copying image:", err);
      });
  };

  // Extract and copy image
  const extractAndCopyImage = () => {
    // Find current image element
    const imageElement = getCurrentImageElement();

    if (imageElement && imageElement.src && imageElement.src !== lastCopiedUrl) {
      copyImageToClipboard(imageElement.src);
      lastCopiedUrl = imageElement.src;
      return;
    }

    // Get container to search in
    const container = currentContentContainer || document.body;

    // Try with images
    const images = Array.from(container.querySelectorAll("img"))
      .filter(img =>
        img.src &&
        !img.src.includes("data:image") &&
        img.src !== lastCopiedUrl &&
        img.src.includes("fbcdn")
      )
      .filter(img => {
        const rect = img.getBoundingClientRect();
        return rect.width >= 100 && rect.height >= 100 && isElementVisible(img);
      })
      .sort((a, b) => {
        const areaA = a.getBoundingClientRect().width * a.getBoundingClientRect().height;
        const areaB = b.getBoundingClientRect().width * b.getBoundingClientRect().height;
        return areaB - areaA; // Largest first
      });

    if (images.length > 0) {
      copyImageToClipboard(images[0].src);
      lastCopiedUrl = images[0].src;
      return;
    }

    // Try background images as last resort
    const backgroundElements = Array.from(container.querySelectorAll("*"));

    for (const el of backgroundElements) {
      const style = window.getComputedStyle(el);
      const bgImage = style.backgroundImage;

      if (
        bgImage &&
        bgImage !== "none" &&
        (bgImage.includes("fbcdn.net") || bgImage.includes("fbsbx.com"))
      ) {
        const imageUrl = bgImage.replace(/^url\(['"](.+)['"]\)$/, "$1");

        if (imageUrl !== lastCopiedUrl) {
          copyImageToClipboard(imageUrl);
          lastCopiedUrl = imageUrl;
          return;
        }
      }
    }

    // Nothing found
    debugLog("No image content found to copy");
  };

  // Create and manage copy button
  const createCopyButton = () => {
    // Add CSS for the button
    const css = `
      #${COPY_BTN_ID} {
        position: fixed;
        top: 70px;
        right: 65px;
        width: 40px;
        height: 40px;
        background-color: rgba(0, 0, 0, 0.7);
        color: white;
        border-radius: 50%;
        z-index: ${CONFIG.buttonZIndex};
        border: none;
        display: none;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        box-shadow: 0 2px 5px rgba(0,0,0,0.3);
        cursor: pointer;
        background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 960 960" fill="white"><path d="M360,720Q327,720 303.5,696.5Q280,673 280,640L280,160Q280,127 303.5,103.5Q327,80 360,80L720,80Q753,80 776.5,103.5Q800,127 800,160L800,640Q800,673 776.5,696.5Q753,720 720,720L360,720ZM360,640L720,640Q720,640 720,640Q720,640 720,640L720,160Q720,160 720,160Q720,160 720,160L360,160Q360,160 360,160Q360,160 360,160L360,640Q360,640 360,640Q360,640 360,640ZM240,840Q207,840 183.5,816.5Q160,793 160,760L160,240L240,240L240,760Q240,760 240,760Q240,760 240,760L600,760L600,840L240,840ZM360,640Q360,640 360,640Q360,640 360,640L360,160Q360,160 360,160Q360,160 360,160L360,160L360,640L360,640Q360,640 360,640Q360,640 360,640Z"/></svg>');
        background-repeat: no-repeat;
        background-position: center;
        background-size: 24px;
      }
      #${COPY_BTN_ID}.visible {
        display: flex !important;
      }
    `;

    const style = document.createElement("style");
    style.textContent = css;
    document.head.appendChild(style);

    // Create button element
    const btn = document.createElement("button");
    btn.id = COPY_BTN_ID;
    btn.setAttribute("aria-label", "Copy image to clipboard");

    btn.addEventListener("click", () => {
      // Reset state
      currentContentContainer = null;
      lastCopiedUrl = null;

      // Find current image and container
      const imageElement = getCurrentImageElement();
      if (imageElement) {
        currentContentContainer = findContentContainer(imageElement);
      }

      extractAndCopyImage();
    });

    document.body.appendChild(btn);

    return btn;
  };

  // Show/hide copy button based on context
  const updateButtonVisibility = () => {
    let btn = document.getElementById(COPY_BTN_ID);
    if (!btn) btn = createCopyButton();

    if (isInContentView()) {
      const imageElement = getCurrentImageElement();

      if (imageElement) {
        currentContentContainer = findContentContainer(imageElement);
        btn.classList.add("visible");
        return;
      }

      // Special case for highlighted stories
      const highlightedContentContainer = document.querySelector(
        'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"]'
      );

      if (highlightedContentContainer) {
        const imageInHighlight = highlightedContentContainer.querySelector(
          'img[src*="fbcdn"]'
        );

        if (imageInHighlight && isElementVisible(imageInHighlight)) {
          currentContentContainer = highlightedContentContainer;
          btn.classList.add("visible");
          return;
        }
      }
    }

    // Hide button if not in relevant view
    btn.classList.remove("visible");
    currentContentContainer = null;
  };

  // Main processing function
  const processPage = () => {
    if (isProcessing) return;
    isProcessing = true;

    try {
      updateButtonVisibility();
    } finally {
      isProcessing = false;
    }
  };

  // Initialize
  const init = () => {
    // Reset state
    currentContentContainer = null;
    lastCopiedUrl = null;

    // Initial check
    processPage();

    // Set up DOM observer
    const observer = new MutationObserver(mutations => {
      const hasRelevantChanges = mutations.some(
        mutation =>
          (mutation.type === "childList" && mutation.addedNodes.length > 0) ||
          (mutation.type === "attributes" &&
            mutation.target.tagName === "IMG")
      );
      if (hasRelevantChanges) processPage();
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ["src", "style", "class"]
    });
  };

  // Start when document is ready
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
