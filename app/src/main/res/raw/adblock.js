(function() {

    if (isDesktopMode()) {
        (function() {
          const selector = 'div.sponsored_ad, article[data-ft*="sponsored_ad"]';

          const removeSponsored = (root = document) => {
            root.querySelectorAll(selector).forEach(el => el.remove());
          };

          removeSponsored();

          const observer = new MutationObserver(mutations => {
            for (const mutation of mutations) {
              for (const node of mutation.addedNodes) {
                if (!(node instanceof HTMLElement)) continue;
                if (node.matches(selector)) {
                  node.remove();
                } else {
                  removeSponsored(node);
                }
              }
            }
          });
          observer.observe(document.body, {
            childList: true,
            subtree: true
          });
        })();

        return;
    }

    const sponsoredTexts = [
        "Sponsored", "Ad", "Gesponsert", "Sponsorlu", "Sponsorowane",
        "Ispoonsara godhameera", "Geborg", "Bersponsor", "Ditaja",
        "Disponsori", "Giisponsoran", "Sponzorováno", "Sponsoreret",
        "Publicidad", "May Sponsor", "Sponsorisée", "Sponsorisé", "Oipytyvôva",
        "Ɗaukar Nayin", "Sponzorirano", "Uterwa inkunga", "Sponsorizzato",
        "Imedhaminiwa", "Hirdetés", "Misy Mpiantoka", "Gesponsord",
        "Sponset", "Patrocinado", "Sponsorizat", "Sponzorované",
        "Sponsoroitu", "Sponsrat", "Được tài trợ", "Χορηγούμενη",
        "Спонсорирано", "Спонзорирано", "Ивээн тэтгэсэн", "Реклама",
        "Спонзорисано", "במימון", "سپانسرڈ", "دارای پشتیبانی مالی",
        "ስፖንሰር የተደረገ", "प्रायोजित", "ተደረገ", "प", "স্পনসর্ড",
        "ਪ੍ਰਯੋਜਿਤ", "પ્રાયોજિત", "ପ୍ରାୟୋଜିତ", "செய்யப்பட்ட செய்யப்பட்ட",
        "చేయబడినది చేయబడినది", "ಪ್ರಾಯೋಜಿಸಲಾಗಿದೆ", "ചെയ്‌തത് ചെയ്‌തത്",
        "ලද ලද ලද", "สนับสนุน สนับสนุน รับ สนับสนุน สนับสนุน",
        "ကြော်ငြာ ကြော်ငြာ", "ឧបត្ថម្ភ ឧបត្ថម្ភ ឧបត្ថម្ភ", "광고",
        "贊助", "赞助内容", "広告", "സ്‌പോൺസർ ചെയ്‌തത്",
        "Anzeige","Peye","Oglas"
    ];

    const specialChar = '󰞋';

    const sponsoredRegex = new RegExp(`(${sponsoredTexts.join('|')})\\s*${specialChar}`, 'i');

    function hideSponsoredContent(config) {
        const { selector, textSelector } = config;
        const containers = document.querySelectorAll(selector);

        containers.forEach(container => {
            const spans = container.querySelectorAll(textSelector);
            for (const span of spans) {
                if (sponsoredRegex.test(span.textContent)) {
                    container.style.display = 'none';
                    break;
                }
            }
        });
    }

    const configs = [
        {
            selector: 'div[data-type="vscroller"] div[data-tracking-duration-id]:has(> div[data-focusable="true"] div[data-mcomponent*="TextArea"] .native-text > span)',
            textSelector: '.native-text > span'
        },
        {
            selector: 'div[data-status-bar-color] > div[data-mcomponent="MContainer"] > div[data-mcomponent="MContainer"]',
            textSelector: 'div[data-mcomponent="TextArea"] .native-text > span'
        },
        {
            selector: 'div[data-mcomponent="MContainer"].m.bg-s3 div[data-mcomponent="MContainer"]',
            textSelector: 'div[data-mcomponent="TextArea"] .native-text > span'
        },
    ];

    function hideAllAds() { configs.forEach(hideSponsoredContent); }

    hideAllAds();

    const observer = new MutationObserver(hideAllAds);
    observer.observe(document.body, { childList: true, subtree: true });

    function containsSponsoredText(text) {
        const lowerText = text.toLowerCase();
        return sponsoredTexts.some(word => {
            const lowerWord = word.toLowerCase();
            // Use word boundary regex to match whole words only
            const wordBoundaryRegex = new RegExp(`\\b${lowerWord.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\b`, 'i');
            return wordBoundaryRegex.test(lowerText);
        });
    }


    function removeReelAds(root = document) {
        const containers = root.querySelectorAll('div.vertically-snappable');

        let hiddenCount = 0;
        containers.forEach((container, index) => {
            // Skip if already hidden
            if (container.dataset.adHidden === 'true') {
                return;
            }

            const spans = container.querySelectorAll('span');

            for (const span of spans) {
                const text = span.textContent;

                if (containsSponsoredText(text)) {

                    // Mark as hidden to prevent re-processing
                    container.dataset.adHidden = 'true';

                    // Completely empty the container but keep it in DOM for scroll snap
                    container.innerHTML = '';

                    // Add a styled message for blocked ad
                    const messageDiv = document.createElement('div');
                    messageDiv.style.cssText = `
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        height: 100%;
                        width: 100%;
                        background: linear-gradient(135deg, #1a1a1a 0%, #000000 100%);
                        color: #666;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                        text-align: center;
                        padding: 20px;
                        box-sizing: border-box;
                    `;

                    const icon = document.createElement('div');
                    icon.style.cssText = `
                        font-size: 48px;
                        margin-bottom: 16px;
                        opacity: 0.6;
                    `;
                    icon.textContent = '🚫';

                    const title = document.createElement('div');
                    title.style.cssText = `
                        font-size: 18px;
                        font-weight: 600;
                        margin-bottom: 8px;
                        color: #888;
                    `;
                    title.textContent = 'Ad Blocked';

                    const subtitle = document.createElement('div');
                    subtitle.style.cssText = `
                        font-size: 14px;
                        color: #555;
                        line-height: 1.4;
                    `;
                    subtitle.textContent = 'Sponsored content was removed';

                    messageDiv.appendChild(icon);
                    messageDiv.appendChild(title);
                    messageDiv.appendChild(subtitle);
                    container.appendChild(messageDiv);

                    // Make container non-interactive
                    container.style.pointerEvents = 'none';
                    container.style.userSelect = 'none';

                    // Set up intersection observer to auto-scroll when this ad comes into view
                    setupAutoScroll(container);

                    hiddenCount++;
                    break;
                }
            }
        });
    }

    // Auto-scroll past hidden ads when they come into view
    function setupAutoScroll(container) {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting && entry.intersectionRatio > 0.5) {
                    // Find next non-hidden reel (skip all consecutive hidden ads)
                    let nextReel = container.nextElementSibling;
                    let skippedCount = 0;

                    while (nextReel && nextReel.dataset.adHidden === 'true') {
                        nextReel = nextReel.nextElementSibling;
                        skippedCount++;
                    }

                    if (nextReel) {
                        // Use a more aggressive scroll approach
                        setTimeout(() => {
                            // Scroll with center alignment for better distance
                            nextReel.scrollIntoView({
                                behavior: 'smooth',
                                block: 'center',
                                inline: 'nearest'
                            });

                            // Additional scroll push after a short delay to ensure we're past the ad
                            setTimeout(() => {
                                window.scrollBy({
                                    top: 100,
                                    behavior: 'smooth'
                                });
                            }, 200);
                        }, 100);
                    }

                    // Unobserve after scrolling once
                    observer.unobserve(container);
                }
            });
        }, {
            threshold: 0.5 // Trigger when 50% of the ad is visible
        });

        observer.observe(container);
    }

    // Initial cleanup
    removeReelAds();

    // Watch for dynamically added reel ads
    const reelObserver = new MutationObserver(mutations => {
        for (const mutation of mutations) {
            for (const node of mutation.addedNodes) {
                if (!(node instanceof HTMLElement)) continue;

                // Check if the added node is a vertically-snappable container or contains one
                if (node.matches('div.vertically-snappable')) {
                    removeReelAds(node.parentElement || document);
                } else if (node.querySelector('div.vertically-snappable')) {
                    removeReelAds(node);
                }
            }
        }
    });

    reelObserver.observe(document.body, { childList: true, subtree: true });
})();
