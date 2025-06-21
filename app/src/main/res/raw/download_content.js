/*
 * Script to add download buttons for stories, stories highlights and reels on Facebook
 * Original Author: @YeiversonYurgaky
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
  let lastDownloadedUrl = null;
  const DOWNLOAD_BTN_ID = "nobook-global-downloader";

  // Selectors for finding media content
  const SELECTORS = {
    mediaElements: [
      'div[role="dialog"] video:not([hidden])',
      'div[role="dialog"] img[src*="fbcdn"]:not([width="16"]):not([hidden])',
      'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"] video',
      'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"] img[src*="fbcdn"]',
      'div[data-pagelet="Story"] video',
      'div[aria-label*="reel"] video',
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
    storyIndicators: [
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
  const debugLog = (...args) => CONFIG.debug && console.log("[ContentDownloader]", ...args);

  const isElementVisible = (element) => {
    const rect = element.getBoundingClientRect();
    return (
      rect.top >= 0 &&
      rect.left >= 0 &&
      rect.bottom <= (window.innerHeight || document.documentElement.clientHeight)
    );
  };

  // Create toast notification
  const createToast = (message, duration = 3000) => {
    const toast = document.createElement("div");
    toast.textContent = message;
    Object.assign(toast.style, {
      position: "fixed",
      bottom: "20px",
      left: "50%",
      transform: "translateX(-50%)",
      backgroundColor: "rgba(0, 0, 0, 0.7)",
      color: "white",
      padding: "8px 16px",
      borderRadius: "20px",
      zIndex: CONFIG.buttonZIndex,
      fontFamily: "sans-serif",
      fontSize: "14px"
    });

    document.body.appendChild(toast);

    if (duration > 0) {
      setTimeout(() => {
        if (document.body.contains(toast)) {
          document.body.removeChild(toast);
        }
      }, duration);
    }

    return toast;
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

  // Get the current visible media element
  const getCurrentMediaElement = () => {
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

    // Fallback: look for any large visible media
    return Array.from(
      document.querySelectorAll('video:not([hidden]), img[src*="fbcdn"]:not([width="16"]):not([hidden])')
    ).find(el => {
      const rect = el.getBoundingClientRect();
      return isElementVisible(el) && rect.width > 150 && rect.height > 150 && el.src;
    });
  };

  // Check if we are in a story or reel view
  const isInStoryOrReelView = () => {
    // URL pattern checks
    const url = window.location.href;
    if (
      url.includes("/stories/") ||
      url.includes("/reel/") ||
      url.includes("/videos/") ||
      url.includes("/watch/?") ||
      url.includes("/photo") ||
      url.includes("/photos/") ||
      url.includes("/highlights/")
    ) {
      return true;
    }

    // Element selectors check
    for (const selector of SELECTORS.storyIndicators) {
      if (document.querySelector(selector)) {
        return true;
      }
    }

    return false;
  };

  // Download media from URL
  const downloadMedia = (url, processingToast) => {
    fetch(url)
      .then(response => response.blob())
      .then(blob => {
        // Remove processing toast
        if (document.body.contains(processingToast)) {
          document.body.removeChild(processingToast);
        }

        if (window.DownloadBridge && window.DownloadBridge.downloadBase64File) {
          const reader = new FileReader();
          reader.onloadend = function() {
            if (reader.result) {
              window.DownloadBridge.downloadBase64File(
                reader.result,
                blob.type || "image/jpeg"
              );
            }
          };
          reader.readAsDataURL(blob);
        }
      })
      .catch(err => {
        if (document.body.contains(processingToast)) {
          document.body.removeChild(processingToast);
        }
        console.error("Error downloading media:", err);
      });
  };

  // Extract and download videos or images
  const extractAndDownloadMedia = () => {
    const processingToast = createToast("Download started...");

    // Find current media element
    const mediaElement = getCurrentMediaElement();

    if (mediaElement && mediaElement.src && mediaElement.src !== lastDownloadedUrl) {
      downloadMedia(mediaElement.src, processingToast);
      lastDownloadedUrl = mediaElement.src;
      return;
    }

    // Get container to search in
    const container = currentContentContainer || document.body;

    // Find videos first
    const videoElement = container.querySelector("video:not([hidden])");
    if (videoElement && videoElement.src && videoElement.src !== lastDownloadedUrl) {
      downloadMedia(videoElement.src, processingToast);
      lastDownloadedUrl = videoElement.src;
      return;
    }

    // If no video, try with images
    const images = Array.from(container.querySelectorAll("img"))
      .filter(img =>
        img.src &&
        !img.src.includes("data:image") &&
        img.src !== lastDownloadedUrl
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
      downloadMedia(images[0].src, processingToast);
      lastDownloadedUrl = images[0].src;
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

        if (imageUrl !== lastDownloadedUrl) {
          downloadMedia(imageUrl, processingToast);
          lastDownloadedUrl = imageUrl;
          return;
        }
      }
    }

    // Nothing found
    if (document.body.contains(processingToast)) {
      document.body.removeChild(processingToast);
    }
    debugLog("No media content found to download");
  };

  // Create and manage download button
  const createDownloadButton = () => {
    // Add CSS for the button
    const css = `
      #${DOWNLOAD_BTN_ID} {
        position: fixed;
        top: 70px;
        right: 15px;
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
        background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 960 960" fill="white"><path d="M480,640L280,440L336,384L440,488L440,160L520,160L520,488L624,384L680,440L480,640ZM240,800Q207,800 183.5,776.5Q160,753 160,720L160,600L240,600L240,720Q240,720 240,720Q240,720 240,720L720,720Q720,720 720,720Q720,720 720,720L720,600L800,600L800,720Q800,753 776.5,776.5Q753,800 720,800L240,800Z"/></svg>');
        background-repeat: no-repeat;
        background-position: center;
        background-size: 24px;
      }
      #${DOWNLOAD_BTN_ID}.visible {
        display: flex !important;
      }
    `;

    const style = document.createElement("style");
    style.textContent = css;
    document.head.appendChild(style);

    // Create button element
    const btn = document.createElement("button");
    btn.id = DOWNLOAD_BTN_ID;
    btn.setAttribute("aria-label", "Download content");

    btn.addEventListener("click", () => {
      // Reset state
      currentContentContainer = null;
      lastDownloadedUrl = null;

      // Find current media and container
      const mediaElement = getCurrentMediaElement();
      if (mediaElement) {
        currentContentContainer = findContentContainer(mediaElement);
      }

      extractAndDownloadMedia();
    });

    document.body.appendChild(btn);

    return btn;
  };

  // Show/hide download button based on context
  const updateButtonVisibility = () => {
    let btn = document.getElementById(DOWNLOAD_BTN_ID);
    if (!btn) btn = createDownloadButton();

    if (isInStoryOrReelView() && !isFeed()) {
      const mediaElement = getCurrentMediaElement();

      if (mediaElement) {
        currentContentContainer = findContentContainer(mediaElement);
        btn.classList.add("visible");
        return;
      }

      // Special case for highlighted stories
      const highlightedStoryContainer = document.querySelector(
        'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"]'
      );

      if (highlightedStoryContainer) {
        const mediaInHighlight = highlightedStoryContainer.querySelector(
          'video, img[src*="fbcdn"]'
        );

        if (mediaInHighlight && isElementVisible(mediaInHighlight)) {
          currentContentContainer = highlightedStoryContainer;
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
    lastDownloadedUrl = null;

    // Initial check
    processPage();

    // Set up DOM observer
    const observer = new MutationObserver(mutations => {
      const hasRelevantChanges = mutations.some(
        mutation =>
          (mutation.type === "childList" && mutation.addedNodes.length > 0) ||
          (mutation.type === "attributes" &&
            (mutation.target.tagName === "VIDEO" ||
             mutation.target.tagName === "IMG"))
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