/*
    Script to add download buttons for stories, stories highlights and reels on Facebook
*/

(function () {
  // Configuration
  const config = {
    buttonZIndex: 999999,
    checkInterval: 1500, // Interval to check for new elements (ms)
    debug: false, // Set to false for production
  };

  // Flag to prevent concurrent processing
  let isProcessing = false;

  // Track if we're viewing a featured story
  let inFeaturedStory = false;

  // Track current content container
  let currentContentContainer = null;

  // Store last downloaded URL to prevent duplicate downloads
  let lastDownloadedUrl = null;

  // Debug logging
  const debugLog = (...args) => {
    if (config.debug) {
      console.log("[ContentDownloader]", ...args);
    }
  };

  // Global download button ID
  const GLOBAL_DOWNLOAD_BTN_ID = "nobook-global-downloader";

  // CSS styles for the download button
  const injectStyles = () => {
    const css = `
            #${GLOBAL_DOWNLOAD_BTN_ID} {
                position: fixed;
                top: 70px;
                right: 15px;
                width: 40px;
                height: 40px;
                background-color: rgba(0, 0, 0, 0.7);
                color: white;
                border-radius: 50%;
                z-index: ${config.buttonZIndex};
                border: none;
                display: none; /* Hidden by default, shown when relevant */
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
            #${GLOBAL_DOWNLOAD_BTN_ID}.visible {
                display: flex !important;
            }
        `;

    const style = document.createElement("style");
    style.textContent = css;
    document.head.appendChild(style);
  };

  // Check if an element is visible in the viewport
  const isElementVisible = (element) => {
    const rect = element.getBoundingClientRect();
    return (
      rect.top >= 0 &&
      rect.left >= 0 &&
      rect.bottom <=
        (window.innerHeight || document.documentElement.clientHeight) &&
      rect.right <= (window.innerWidth || document.documentElement.clientWidth)
    );
  };

  // Find the appropriate container for the content
  const findAndSetContentContainer = (element) => {
    if (!element) return;

    // Reset the current container first
    currentContentContainer = null;

    // Try to find the closest content container
    let container =
      element.closest('div[role="dialog"]') ||
      element.closest('div[data-pagelet="Story"]') ||
      element.closest('div[aria-label*="story"]') ||
      element.closest(".story-viewer") ||
      element.closest(".story_viewer") ||
      element.closest(
        'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"]'
      ) ||
      element.closest('div[data-pagelet="ProfilePhoto"]') ||
      element.closest('div[aria-label*="photo"]') ||
      element.closest('div[data-pagelet*="ProfileAppSection"]');

    // If no specific container found, use parent
    if (!container) {
      container = element.parentElement;
    }

    currentContentContainer = container;
    debugLog("Content container set:", currentContentContainer);
  };

  // Get the current visible media element
  const getCurrentMediaElement = () => {
    // List of possible media selectors, in order of priority
    const mediaSelectors = [
      // Dialogs (stories, highlights)
      'div[role="dialog"] video:not([hidden])',
      'div[role="dialog"] img[src*="fbcdn"]:not([width="16"]):not([hidden])',

      // Highlighted story
      'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"] video',
      'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"] img[src*="fbcdn"]',

      // Reels
      'div[data-pagelet="Story"] video',
      'div[aria-label*="reel"] video',

      // Profile photos
      'div[data-pagelet="ProfilePhoto"] img[src*="fbcdn"]',
    ];

    // Try each selector
    for (const selector of mediaSelectors) {
      const elements = document.querySelectorAll(selector);

      // Find the first visible element
      for (const element of elements) {
        if (isElementVisible(element) && element.src) {
          return element;
        }
      }
    }

    // If no element found with specific selectors, look for any large visible media
    return Array.from(
      document.querySelectorAll(
        'video:not([hidden]), img[src*="fbcdn"]:not([width="16"]):not([hidden])'
      )
    ).find((el) => {
      const rect = el.getBoundingClientRect();
      return (
        isElementVisible(el) && rect.width > 150 && rect.height > 150 && el.src
      );
    });
  };

  // Extract and download videos or images
  const extractAndDownloadMedia = () => {
    // Always reset container for new download
    const currentMediaElement = getCurrentMediaElement();

    if (currentMediaElement) {
      findAndSetContentContainer(currentMediaElement);
    }

    // Show initial processing message
    const processingToast = document.createElement("div");
    processingToast.textContent = "Processing download...";
    Object.assign(processingToast.style, {
      position: "fixed",
      bottom: "20px",
      left: "50%",
      transform: "translateX(-50%)",
      backgroundColor: "rgba(0, 0, 0, 0.7)",
      color: "white",
      padding: "8px 16px",
      borderRadius: "20px",
      zIndex: config.buttonZIndex,
      fontFamily: "sans-serif",
      fontSize: "14px",
    });
    document.body.appendChild(processingToast);

    // Get the current media element again to ensure we have the most up-to-date one
    const mediaToDownload = getCurrentMediaElement();

    if (mediaToDownload && mediaToDownload.src) {
      // Check if this URL was already downloaded to prevent duplicates
      if (mediaToDownload.src === lastDownloadedUrl) {
        debugLog("Preventing duplicate download of the same content");
        // Force find a different media element that's not the same as last downloaded
        const allMedia = Array.from(
          document.querySelectorAll(
            'video:not([hidden]), img[src*="fbcdn"]:not([width="16"]):not([hidden])'
          )
        ).filter((el) => isElementVisible(el) && el.src !== lastDownloadedUrl);

        if (allMedia.length > 0) {
          downloadMedia(allMedia[0].src, processingToast);
          lastDownloadedUrl = allMedia[0].src; // Update last downloaded URL
        } else {
          // Remove processing toast if nothing found
          if (document.body.contains(processingToast)) {
            document.body.removeChild(processingToast);
          }
          debugLog("No new media found to download");
        }
      } else {
        downloadMedia(mediaToDownload.src, processingToast);
        lastDownloadedUrl = mediaToDownload.src; // Update last downloaded URL
      }
      return;
    }

    // Use current container or fallback to body if no direct media found
    const container = currentContentContainer || document.body;
    debugLog("Searching for media in container:", container);

    // Find videos first, then images within the container
    const videoElement = container.querySelector("video:not([hidden])");

    if (videoElement && videoElement.src) {
      downloadMedia(videoElement.src, processingToast);
      lastDownloadedUrl = videoElement.src; // Update last downloaded URL
    } else {
      // If no video, try with images
      const images = Array.from(container.querySelectorAll("img"))
        .filter(
          (img) =>
            img.src &&
            !img.src.includes("data:image") &&
            img.src !== lastDownloadedUrl
        )
        .filter((img) => {
          const rect = img.getBoundingClientRect();
          return (
            rect.width >= 100 && rect.height >= 100 && isElementVisible(img)
          );
        })
        .sort((a, b) => {
          const rectA = a.getBoundingClientRect();
          const rectB = b.getBoundingClientRect();
          const areaA = rectA.width * rectA.height;
          const areaB = rectB.width * rectB.height;
          return areaB - areaA; // Largest first
        });

      // Try with the largest image first
      if (images.length > 0) {
        downloadMedia(images[0].src, processingToast);
        lastDownloadedUrl = images[0].src; // Update last downloaded URL
      } else {
        // Try background images as last resort
        const backgroundElements = Array.from(container.querySelectorAll("*"));
        let foundBackgroundImage = false;

        for (const el of backgroundElements) {
          const style = window.getComputedStyle(el);
          const bgImage = style.backgroundImage;

          if (
            bgImage &&
            bgImage !== "none" &&
            (bgImage.includes("fbcdn.net") || bgImage.includes("fbsbx.com"))
          ) {
            const imageUrl = bgImage.replace(/^url\(['"](.+)['"]\)$/, "$1");

            // Skip if this is the same URL we just downloaded
            if (imageUrl === lastDownloadedUrl) continue;

            foundBackgroundImage = true;
            downloadMedia(imageUrl, processingToast);
            lastDownloadedUrl = imageUrl; // Update last downloaded URL
            break;
          }
        }

        if (!foundBackgroundImage) {
          // Remove processing toast if nothing found
          if (document.body.contains(processingToast)) {
            document.body.removeChild(processingToast);
          }
          debugLog("No media content found to download");
        }
      }
    }
  };

  // Download media from URL
  const downloadMedia = (url, processingToast) => {
    fetch(url)
      .then((response) => response.blob())
      .then((blob) => {
        // Remove processing toast
        if (document.body.contains(processingToast)) {
          document.body.removeChild(processingToast);
        }

        if (window.DownloadBridge && window.DownloadBridge.downloadBase64File) {
          const reader = new FileReader();
          reader.onloadend = function () {
            if (reader.result) {
              window.DownloadBridge.downloadBase64File(
                reader.result,
                blob.type || "image/jpeg"
              );
              showSuccessToast();
            }
          };
          reader.readAsDataURL(blob);
        }
      })
      .catch((err) => {
        // Remove processing toast
        if (document.body.contains(processingToast)) {
          document.body.removeChild(processingToast);
        }
        console.error("Error downloading media:", err);
      });
  };

  // Show a success toast notification
  const showSuccessToast = () => {
    const toast = document.createElement("div");
    toast.textContent = "Content downloaded successfully";
    Object.assign(toast.style, {
      position: "fixed",
      bottom: "20px",
      left: "50%",
      transform: "translateX(-50%)",
      backgroundColor: "rgba(0, 0, 0, 0.7)",
      color: "white",
      padding: "8px 16px",
      borderRadius: "20px",
      zIndex: config.buttonZIndex,
      fontFamily: "sans-serif",
      fontSize: "14px",
    });

    document.body.appendChild(toast);

    // Remove after 3 seconds
    setTimeout(() => {
      if (document.body.contains(toast)) {
        document.body.removeChild(toast);
      }
    }, 3000);
  };

  // Check if we are in a story or reel view
  const isInStoryOrReelView = () => {
    const url = window.location.href;

    // URL pattern checks
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
    const selectors = [
      // Dialog with large media
      'div[role="dialog"] video:not([hidden]), div[role="dialog"] img[src*="fbcdn"]:not([width="16"]):not([hidden])',
      // Story selectors
      'div[data-sigil="story-viewer"]',
      'div[data-sigil="story-popup-header"]',
      'div[data-sigil="story-tray-item"]',
      ".story_body_container",
      ".story_viewer",
      ".story-container",
      // Highlight indicators
      'div[aria-label*="highlight"]',
      'div[aria-label*="Highlight"]',
      // User provided highlight selector
      'div.x1ey2m1c.x9f619.xds687c.x17qophe.x10l6tqk.x13vifvy[role="presentation"]',
      // Profile photo
      'div[data-pagelet="ProfilePhoto"]',
    ];

    // If any selector is found, we're in a story/reel view
    for (const selector of selectors) {
      if (document.querySelector(selector)) {
        return true;
      }
    }

    return false;
  };

  // Check if we're in search mode
  const isInSearchMode = () => {
    if (window.location.href.includes("/search/")) {
      return true;
    }

    const searchSelectors = [
      'input[type="search"]:focus',
      'div[role="dialog"][aria-label*="Search"]',
      'form[action="/search/"]',
      'div[aria-label="Search results"]',
      'input[placeholder*="Search"]',
    ];

    for (const selector of searchSelectors) {
      if (document.querySelector(selector)) {
        return true;
      }
    }

    return false;
  };

  // Detect if we're on the main feed
  const isMainFeed = () => {
    if (
      window.location.href.match(/facebook\.com\/?$/) ||
      window.location.href.match(/facebook\.com\/home(\.php)?\/?$/)
    ) {
      return true;
    }

    const feedSelectors = [
      'div[aria-label="Create Story"]',
      'div[data-pagelet="Stories"]',
      'div[aria-label="Create Post"]',
      'div[role="main"] form',
      'div[role="tablist"][aria-label="News Feed"]',
    ];

    for (const selector of feedSelectors) {
      if (document.querySelector(selector)) {
        return true;
      }
    }

    return false;
  };

  // Show the download button
  const showDownloadButton = () => {
    let btn = document.getElementById(GLOBAL_DOWNLOAD_BTN_ID);

    if (!btn) {
      btn = document.createElement("button");
      btn.id = GLOBAL_DOWNLOAD_BTN_ID;
      btn.setAttribute("aria-label", "Download content");

      // Ensure button visibility
      Object.assign(btn.style, {
        zIndex: config.buttonZIndex,
        visibility: "visible",
        opacity: "1",
        display: "flex",
      });

      btn.addEventListener("click", () => {
        // Force reset of container and recapture of current media
        currentContentContainer = null;
        lastDownloadedUrl = null;

        // Always find the current visible media before downloading
        const currentMediaElement = getCurrentMediaElement();
        if (currentMediaElement) {
          findAndSetContentContainer(currentMediaElement);
        }

        extractAndDownloadMedia();
      });

      document.body.appendChild(btn);
    }

    btn.classList.add("visible");
  };

  // Hide the download button
  const hideDownloadButton = () => {
    const btn = document.getElementById(GLOBAL_DOWNLOAD_BTN_ID);
    if (btn) {
      btn.classList.remove("visible");
      btn.style.display = "none";
    }
  };

  // Update button visibility based on content
  const updateButtonVisibility = () => {
    // Don't show in search mode or main feed
    if (isInSearchMode() || isMainFeed()) {
      hideDownloadButton();
      inFeaturedStory = false;
      currentContentContainer = null;
      return;
    }

    // Check if we're in a relevant view
    if (isInStoryOrReelView()) {
      // Try to find current visible media
      const currentMediaElement = getCurrentMediaElement();

      if (currentMediaElement) {
        findAndSetContentContainer(currentMediaElement);
        showDownloadButton();
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
          showDownloadButton();
          return;
        }
      }
    }

    // By default, hide the button
    hideDownloadButton();
    inFeaturedStory = false;
    currentContentContainer = null;
  };

  // Process the page
  const processPage = () => {
    if (isProcessing) return;
    isProcessing = true;

    try {
      updateButtonVisibility();
    } finally {
      isProcessing = false;
    }
  };

  // Initialize everything
  const init = () => {
    injectStyles();

    // Set up video playback monitoring
    document.addEventListener(
      "play",
      (e) => {
        if (e.target.tagName.toLowerCase() === "video") {
          const videoElement = e.target;
          const rect = videoElement.getBoundingClientRect();

          if (
            rect.width > 150 &&
            rect.height > 150 &&
            isElementVisible(videoElement)
          ) {
            inFeaturedStory = true;
            findAndSetContentContainer(videoElement);
            showDownloadButton();
          }
        }
      },
      true
    );

    // Reset state
    inFeaturedStory = false;
    currentContentContainer = null;
    lastDownloadedUrl = null;

    // Run initial check
    processPage();

    // Set up DOM observer
    const observer = new MutationObserver((mutations) => {
      const hasRelevantChanges = mutations.some(
        (mutation) =>
          (mutation.type === "childList" && mutation.addedNodes.length > 0) ||
          (mutation.type === "attributes" &&
            (mutation.target.tagName === "VIDEO" ||
              mutation.target.tagName === "IMG"))
      );

      if (hasRelevantChanges) {
        processPage();
      }
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ["src", "style", "class"],
    });

    // Check periodically
    setInterval(processPage, config.checkInterval);

    // Check when URL changes
    let lastUrl = window.location.href;
    setInterval(() => {
      if (lastUrl !== window.location.href) {
        lastUrl = window.location.href;

        // Reset state and check again
        inFeaturedStory = false;
        currentContentContainer = null;
        lastDownloadedUrl = null;
        setTimeout(processPage, 500);
      }
    }, 1000);

    // Check on scroll (with debounce)
    window.addEventListener(
      "scroll",
      () => {
        clearTimeout(window.scrollTimer);
        window.scrollTimer = setTimeout(processPage, 300);
      },
      { passive: true }
    );
  };

  // Start when the document is ready
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
