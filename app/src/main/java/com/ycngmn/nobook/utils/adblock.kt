package com.ycngmn.nobook.utils

val sponsoredAdBlockerScript = """
    (function() {
        // list of sponsored text variations
        const sponsoredTexts = [
            "Sponsored", "Gesponsert", "Sponsorlu", "Sponsorowane",
            "Ispoonsara godhameera", "Geborg", "Bersponsor", "Ditaja",
            "Disponsori", "Giisponsoran", "Sponzorováno", "Sponsoreret",
            "Publicidad", "May Sponsor", "Sponsorisée", "Oipytyvôva",
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
            "贊助", "赞助内容", "広告", "സ്‌പോൺസർ ചെയ്‌തത്"
        ];
    
     
        const sponsoredRegex = new RegExp(sponsoredTexts.join('|'), 'i');
    
        function blockSponsoredContent(config = {}) {
            const {
                selector = 'div', // Default selector
                textSelector = 'div[data-mcomponent="TextArea"] .native-text > span', // Default text selector
                hideMethod = 'hide', // 'hide' (display: none) or 'reposition' (CSS positioning)
                validateContainer = () => true // Optional validation function for containers
            } = config;
    
            const containers = document.querySelectorAll(selector);
    
            containers.forEach(container => {
                const spans = container.querySelectorAll(textSelector);
                for (const span of spans) {
                    if (sponsoredRegex.test(span.textContent) && validateContainer(container)) {
                        if (hideMethod === 'reposition') {
                            container.style.position = 'absolute';
                            container.style.left = '-3000px';
                        } else {
                            container.style.display = 'none';
                        }
                        break;
                    }
                }
            });
        }
    
        const filterConfig = {
            selector: 'div[data-type="vscroller"] div[data-tracking-duration-id]:has(> div[data-focusable="true"] div[data-mcomponent*="TextArea"] .native-text > span)',
            textSelector: '.native-text > span',
            hideMethod: 'reposition',
            validateContainer: () => true
        };
    
        const scriptConfig = {
            selector: 'div[data-status-bar-color] > div[data-mcomponent="MContainer"] > div[data-mcomponent="MContainer"]',
            textSelector: 'div[data-mcomponent="TextArea"] .native-text > span',
            hideMethod: 'hide',
            validateContainer: (container) => !container.querySelector('[data-tracking-duration-id]')
        };
    
        function blockAllAds() {
            blockSponsoredContent(filterConfig);
            blockSponsoredContent(scriptConfig);
        }
    
        blockAllAds();
    
        const observer = new MutationObserver(blockAllAds);
        observer.observe(document.body, { childList: true, subtree: true });
    })();
    """.trimIndent()