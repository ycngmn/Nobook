(function() {
  const colorRegex = /background-color\s*:\s*(#242526|rgba\s*\(\s*36\s*,\s*37\s*,\s*38\s*,\s*1\.?0*\s*\)|rgba\s*\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*1\.?0*\s*\)|#([0-9a-fA-F]{6}))\s*;/gi;

  function isLightGray(r, g, b) {
    return r >= 50 && r <= 150 && g >= 50 && g <= 150 && b >= 50 && b <= 150 &&
           Math.abs(r - g) <= 20 && Math.abs(g - b) <= 20 && Math.abs(r - b) <= 20;
  }

  function processStyles() {
    const meta = document.querySelector('meta[name="theme-color"]');
    if (meta?.getAttribute('content')?.toLowerCase().trim() !== '#242526') return;
    meta?.setAttribute('content', '#000000');

    document.querySelectorAll('style').forEach(style => {
      if (style.sheet?.cssRules) {
        try {
          Array.from(style.sheet.cssRules).forEach(rule => {
            const bg = rule.style?.backgroundColor.replace(/\s+/g, '').toLowerCase();
            if (bg === '#242526' || bg === 'rgba(36,37,38,1)' || bg === 'rgba(36,37,38,1.0)') {
              rule.style.backgroundColor = '#000000';
            } else if (bg.startsWith('rgba(')) {
              const match = bg.match(/rgba\((\d+),(\d+),(\d+),1\.?0*\)/);
              if (match && isLightGray(...match.slice(1).map(Number))) {
                rule.style.backgroundColor = '#121212';
              }
            } else if (bg.startsWith('#') && bg.length === 7) {
              const [r, g, b] = [1, 3, 5].map(i => parseInt(bg.slice(i, i + 2), 16));
              if (isLightGray(r, g, b)) rule.style.backgroundColor = '#121212';
            }
          });
        } catch (e) {}
      }

      if (style.innerHTML) {
        style.innerHTML = style.innerHTML.replace(colorRegex, (m, g, r, g2, b, hex) => {
          if (g === '#242526' || g === 'rgba(36,37,38,1.0)') return 'background-color:#000000;';
          if (r && g2 && b && isLightGray(+r, +g2, +b)) return 'background-color:#121212;';
          if (hex && isLightGray(...[0, 2, 4].map(i => parseInt(hex.slice(i, i + 2), 16)))) {
            return 'background-color:#121212;';
          }
          return m;
        });
      }
    });

    document.querySelectorAll('[style*="background-color"]').forEach(el => {
      const style = el.getAttribute('style').replace(colorRegex, (m, g, r, g2, b, hex) => {
        if (g === '#242526' || g === 'rgba(36,37,38,1.0)') return 'background-color:#000000;';
        if (r && g2 && b && isLightGray(+r, +g2, +b)) return 'background-color:#121212;';
        if (hex && isLightGray(...[0, 2, 4].map(i => parseInt(hex.slice(i, i + 2), 16)))) {
          return 'background-color:#121212;';
        }
        return m;
      });
      if (style !== el.getAttribute('style')) el.setAttribute('style', style);
    });
  }

  processStyles();

  new MutationObserver(mutations => {
    if (mutations.some(m =>
      (m.type === 'childList' && Array.from(m.addedNodes).some(n =>
        n.tagName === 'STYLE' || (n.nodeType === 1 && n.hasAttribute('style')) ||
        (n.tagName === 'META' && n.getAttribute('name') === 'theme-color'))) ||
      (m.type === 'characterData' && m.target.parentNode?.tagName === 'STYLE') ||
      (m.type === 'attributes' && (m.attributeName === 'style' ||
        (m.target.tagName === 'META' && m.attributeName === 'content'))))
    ) processStyles();
  }).observe(document.documentElement, {
    childList: true,
    subtree: true,
    characterData: true,
    attributes: true,
    attributeFilter: ['style', 'content']
  });
})();