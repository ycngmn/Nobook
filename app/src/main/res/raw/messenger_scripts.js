(function () {
  const hideAndInsert = () => {

    if (!window.location.href.includes("www.facebook.com/messages")) {
        if (document.getElementById('custom-fb-button'))
            document.getElementById('custom-fb-button').remove();
        return;
    }

    // Hide fb items on topBar.
    document.querySelectorAll('.x6s0dn4.x78zum5.x5yr21d.xl56j7k.x1emribx')
      .forEach(el => el.style.display = 'none');

    const label = document.querySelector('label:first-of-type');
    if (label) label.style.display = 'none';

    // Add custom button to navigate to Facebook
    const link = document.querySelector('a[aria-hidden="false"][aria-label="Facebook"]');
    if (link && !document.getElementById('custom-fb-button')) {
      const btn = document.createElement('button');
      btn.id = 'custom-fb-button';

      const r = link.getBoundingClientRect();
      Object.assign(btn.style, {
        position: 'absolute',
        width: '40px',
        height: '40px',
        opacity: '0',
        cursor: 'pointer',
        zIndex: '9999',
        border: 'none',
        background: 'transparent',
        left: `${r.left + scrollX}px`,
        top: `${r.top + scrollY}px`
      });

      btn.onclick = () => {
        if (typeof NavigateBridge !== 'undefined' && NavigateBridge.onNavigateFB)
          NavigateBridge.onNavigateFB();
      };
      document.body.appendChild(btn);
    }
  };

  hideAndInsert();

  new MutationObserver(hideAndInsert)
    .observe(document.documentElement || document.body, { childList: true, subtree: true });
})();
