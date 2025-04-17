package com.ycngmn.nobook.utils

val sponsoredAdBlockerScript = """
    (function() {
        function blockAds() {
            // Target language variations of "Sponsored"
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
            const containers = document.querySelectorAll('div[data-status-bar-color] > div[data-mcomponent="MContainer"] > div[data-mcomponent="MContainer"]');
            
            containers.forEach(container => {
                const spans = container.querySelectorAll('div[data-mcomponent="TextArea"] .native-text > span');

                for (const span of spans) {
                    if (sponsoredRegex.test(span.textContent)) {
                        const hasTrackingDuration = container.querySelector('[data-tracking-duration-id]');
                        if (!hasTrackingDuration) {
                            container.style.display = 'none';
                            break;
                        }
                    }
                }
            });
        }
        
        blockAds();

        const observer = new MutationObserver(function(mutations) {
            blockAds();
        });

        observer.observe(document.body, { childList: true, subtree: true });
    })();
    """.trimIndent()