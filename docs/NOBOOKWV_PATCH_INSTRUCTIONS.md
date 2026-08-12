# Hướng dẫn vá NobookWV.kt cho tính năng Anti-Reload (bản đã sửa đúng)

## Vì sao bản trước sai?
Bản hướng dẫn trước giả định app dùng `android.webkit.WebViewClient` với
`onPageStarted`/`onPageFinished`. Sau khi đọc được nội dung thật của
`NobookWV.kt`, xác nhận Nobook dùng thư viện **compose-webview-multiplatform**
(`com.multiplatform.webview.web.*`). Cách nạp script đúng của thư viện này là
qua `navigator.evaluateJavaScript(script) { callback }`, gọi trong một
`LaunchedEffect` theo dõi `state.loadingState`. Không có `WebViewClient` lộ ra
để override trực tiếp trong file này.

## Bước 1 — Thêm hằng số script ở đầu file (ngoài hàm NobookWebView)
Dán ngay sau dòng `import kotlinx.coroutines.delay` (trước dòng `@Composable`):

```kotlin
private const val ANTI_RELOAD_SCRIPT = """
(function () {
  try {
    if (window.__nobookAntiReloadActive) return;
    window.__nobookAntiReloadActive = true;

    var defineAlways = function (obj, prop, value) {
      try {
        Object.defineProperty(obj, prop, { configurable: true, get: function () { return value; } });
      } catch (e) {}
    };

    defineAlways(document, "visibilityState", "visible");
    defineAlways(document, "hidden", false);
    defineAlways(document, "webkitVisibilityState", "visible");
    defineAlways(document, "webkitHidden", false);

    var blocked = ["visibilitychange", "webkitvisibilitychange", "blur", "pagehide", "freeze"];
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

    Object.defineProperty(document, "hasFocus", { configurable: true, value: function () { return true; } });

    console.info("[Nobook] Anti-Reload guard active");
  } catch (err) {
    console.error("[Nobook] Anti-Reload injection failed:", err);
  }
})();
"""
```
Không cần kiểm tra domain vì Nobook chỉ dùng WebView cho Facebook.

## Bước 2 — Thêm LaunchedEffect mới, ngay sau block có sẵn
Tìm block có sẵn:
```kotlin
val userScripts by viewModel.scripts
val loadingState = state.loadingState

LaunchedEffect(loadingState, userScripts) {
    if (loadingState is LoadingState.Finished) {
        userScripts?.let { scripts ->
            navigator.evaluateJavaScript(scripts) {
                isLoading = false
            }
        }
    }
}
```
Thêm ngay dưới nó (chỉ thêm, không sửa dòng nào ở trên):
```kotlin
LaunchedEffect(loadingState) {
    if (loadingState is LoadingState.Finished) {
        navigator.evaluateJavaScript(ANTI_RELOAD_SCRIPT) {}
    }
}
```

## Bước 3 — Dọn dẹp (tuỳ chọn)
`AntiReloadInjector.kt` và `anti_reload.js` (tạo theo giả định sai trước đó)
không được gọi tới ở đâu — an toàn khi build (dead code) nhưng có thể xoá.

## Bước 4 — Build & tag v1.0.0
```bash
git add app/src/main/java/com/ycngmn/nobook/ui/screens/NobookWV.kt
git commit -m "Wire Anti-Reload guard into NobookWebView via evaluateJavaScript on LoadingState.Finished"
git push origin main

git tag -a v1.0.0 -m "Nobook v1.0.0 - Anti-Reload guard + Material You groundwork"
git push origin v1.0.0
```
Push tag `v1.0.0` kích hoạt `.github/workflows/create-release.yml` có sẵn —
build + ký + phát hành APK Release tại
https://github.com/adv247/Nobook/releases

## Kiểm chứng
Mở Facebook → cuộn newsfeed → khoá màn hình/chuyển app 30-60s → mở lại.
Newsfeed phải giữ nguyên vị trí scroll, không tự nhảy về đầu.
