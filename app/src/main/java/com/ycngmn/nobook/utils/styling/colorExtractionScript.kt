package com.ycngmn.nobook.utils.styling

val colorExtractionScript =  """
    (function() {
        const meta = document.querySelector('meta[name="theme-color"]');
        const notify = () => window.ThemeBridge?.onThemeColorChanged?.(meta?.content ?? "null");
        if (meta) {
            notify();
            new MutationObserver(() => notify())
                .observe(meta, { attributes: true, attributeFilter: ['content'] });
        }
    })();
""".trimIndent()