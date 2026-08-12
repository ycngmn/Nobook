(function () {
  try {
    var host = window.location.hostname || "";
    var targets = [
      "facebook.com",
      "m.facebook.com",
      "www.facebook.com",
      "web.facebook.com",
      "fbsbx.com",
      "www.fbsbx.com"
    ];

    var isTarget = targets.some(function (h) {
      return host === h || host.endsWith("." + h);
    });

    if (!isTarget) return;
    if (window.__nobookAntiReloadActive) return;
    window.__nobookAntiReloadActive = true;

    var defineAlways = function (obj, prop, value) {
      try {
        Object.defineProperty(obj, prop, {
          configurable: true,
          get: function () { return value; }
        });
      } catch (e) { /* ignore */ }
    };

    defineAlways(document, "visibilityState", "visible");
    defineAlways(document, "hidden", false);
    defineAlways(document, "webkitVisibilityState", "visible");
    defineAlways(document, "webkitHidden", false);
    defineAlways(document, "mozHidden", false);
    defineAlways(document, "msHidden", false);

    var blocked = [
      "visibilitychange",
      "webkitvisibilitychange",
      "mozvisibilitychange",
      "msvisibilitychange",
      "blur",
      "pagehide",
      "freeze"
    ];

    var origAdd = EventTarget.prototype.addEventListener;
    var origDispatch = EventTarget.prototype.dispatchEvent;

    EventTarget.prototype.addEventListener = function (type, listener, options) {
      if (blocked.indexOf(type) !== -1) return;
      return origAdd.call(this, type, listener, options);
    };

    EventTarget.prototype.dispatchEvent = function (evt) {
      if (evt && blocked.indexOf(evt.type) !== -1) return true;
      return origDispatch.call(this, evt);
    };

    window.onblur = null;
    window.onpagehide = null;
    document.onvisibilitychange = null;
    document.onwebkitvisibilitychange = null;

    Object.defineProperty(document, "hasFocus", {
      configurable: true,
      value: function () { return true; }
    });

    console.info("[Nobook] Anti-Reload guard active on " + host);
  } catch (err) {
    console.error("[Nobook] Anti-Reload injection failed:", err);
  }
})();
