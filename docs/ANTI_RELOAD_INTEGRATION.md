# Anti-Reload + Material You Integration — Lifecycle Report
Branch: feature/anti-reload-materialyou · Base: main · Repo: adv247/Nobook

Tài liệu này đi qua đầy đủ vòng đời xử lý thay đổi theo yêu cầu:
Debug → So sánh → Phân tích luồng hoạt động → Kiểm thử → Sửa lỗi → Soi chéo →
Kiểm chứng → Đánh giá chất lượng → Tài liệu hóa → Cải tiến → Ngăn lỗi tái diễn →
Báo cáo tối ưu → Đánh giá cuối cùng.

## 1. Debug (khảo sát hiện trạng)
- Xác nhận qua GitHub connector: repo `adv247/Nobook` tồn tại, nhánh mặc định `main`, commit đầu `608296b441d3b98beb15f8504d4768cc89b7566c`.
- Liệt kê trực tiếp cấu trúc thật: `app/src/main/java/com/ycngmn/nobook/` gồm `MainActivity.kt`, `data/`, `ui/{components,screens,theme,viewmodel}/`, `utils/`; `res/raw/` có sẵn 12 file JS (`adblock.js`, `amoled_black.js`, `scripts.js`, `sticky_navbar.js`...); `.github/workflows/create-release.yml` đã có sẵn quy trình build + ký APK khi push tag `v*.*.*`.
- Hạn chế công cụ phát hiện được: `get_file_contents` của GitHub connector trong phiên làm việc này chỉ trả về metadata (tên, SHA, size) cho file, không trả nội dung thô của file code (đã kiểm chứng lại với file 6 byte `app/.gitignore`). Vì vậy nội dung chính xác từng dòng của `NobookWV.kt` (10148 bytes) chưa được đọc trực tiếp trong phiên này.

## 2. So sánh (baseline vs thay đổi đề xuất)
| Hạng mục | Hiện trạng (main) | Đề xuất trong PR |
|---|---|---|
| CI/CD | `create-release.yml` chỉ chạy khi push tag `v*.*.*`, ký APK | Thêm `ci-build-artifacts.yml` chạy trên mọi push/PR để build debug + xuất Artifact, KHÔNG đụng workflow release |
| Script JS | 12 script có sẵn nạp qua `fetchScripts.kt`/`scripts.js` | Thêm `anti_reload.js`, `material_you.js` — file mới, không đè file cũ |
| Kotlin | `NobookWV.kt` xử lý `onPageStarted/onPageFinished` | Thêm class mới `AntiReloadInjector.kt` (độc lập) + cần merge tay 6 dòng vào `NobookWV.kt` (mục 9) |

Quyết định thiết kế: đặt tên workflow mới `ci-build-artifacts.yml` (khác `create-release.yml`) để tránh trùng trigger, tránh phá vỡ pipeline release đang hoạt động.

## 3. Phân tích luồng hoạt động (flow)
1. WebView load facebook.com → `onPageStarted` được gọi.
2. Facebook bootstrap JS chạy, gắn listener `visibilitychange/blur/pagehide` để phát hiện tab mất focus → gọi API refresh newsfeed.
3. Nếu `AntiReloadInjector.inject()` được gọi TRƯỚC bước 2 (tại `onPageStarted`), override `document.hidden/visibilityState` và chặn `addEventListener/dispatchEvent` cho nhóm event trên có hiệu lực trước khi FB gắn listener — vô hiệu hóa cơ chế tự refresh.
4. Gọi lại lần 2 tại `onPageFinished`/`onPageCommitVisible` phòng trường hợp FB tải lại bundle JS qua AJAX (SPA navigation).
5. Script tự nhận diện domain nên không ảnh hưởng site khác mở trong app.

## 4. Kiểm thử (Test)
Không chạy được `./gradlew assembleDebug` thật trong sandbox (không có Android SDK/JDK, không có mạng để tải Gradle deps). Đã thực hiện kiểm thử tĩnh khả thi: lint cú pháp JS/Kotlin thủ công, validate YAML, kiểm tra tên resource `anti_reload`/`material_you` không trùng 12 file JS đã có. Kiểm thử build thật sẽ được xác nhận khi GitHub Actions (`ci-build-artifacts.yml`) chạy trên PR này.

## 5. Sửa lỗi (Fix)
- Bọc try/catch toàn bộ script để không throw ra ngoài làm crash WebView.
- Thêm điều kiện kiểm tra hostname ngay đầu script, return sớm nếu không phải domain Facebook/Messenger — tránh chặn nhầm code trang khác.
- Thêm cờ `window.__nobookAntiReloadActive` để tránh patch chồng patch khi inject nhiều lần.

## 6. Soi chéo (Cross-review)
Đối chiếu logic với 3 extension người dùng cung cấp (Always Active Window, facebook-no-reload của diepvantien, J2TEAM Security) — cả ba dựa trên đúng 2 cơ chế: giả lập visibility API và chặn sự kiện blur/visibilitychange. PR này áp dụng đúng 2 cơ chế đó ở tầng WebView. Đối chiếu với kiến trúc Nobook (res/raw/*.js nạp qua WebView.evaluateJavascript) — cách làm mới nhất quán với pattern hiện có của repo.

## 7. Kiểm chứng (Verify)
Sau khi merge: mở tab Checks trên PR → workflow `ci-build-artifacts.yml` chạy `assembleDebug`; nếu xanh, tải APK debug ở Artifacts, cài lên máy Android, mở Facebook, tắt màn hình/chuyển app 30s rồi mở lại — newsfeed phải giữ nguyên vị trí scroll, không tự nhảy về đầu.

## 8. Đánh giá chất lượng (Quality assessment)
Script chỉ áp dụng đúng 5 domain yêu cầu; injection là 1 lần `evaluateJavascript` nhỏ (~2KB), không có `setInterval` tốn CPU; không `eval()` trên dữ liệu động, không gọi API ngoài, không thu thập dữ liệu.

## 9. Tài liệu hóa (Documentation) — bước merge tay cần làm
Mở `app/src/main/java/com/ycngmn/nobook/ui/screens/NobookWV.kt`, trong WebViewClient thêm:
```kotlin
import com.ycngmn.nobook.utils.AntiReloadInjector

override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
    super.onPageStarted(view, url, favicon)
    AntiReloadInjector.inject(view.context, view, url?.let { Uri.parse(it).host })
}

override fun onPageFinished(view: WebView, url: String?) {
    super.onPageFinished(view, url)
    AntiReloadInjector.inject(view.context, view, url?.let { Uri.parse(it).host })
}
```
(Chỉ thêm, không xóa dòng nào của code injection hiện có.)

## 10. Cải tiến (Improve)
Đề xuất PR kế tiếp: thêm toggle On/Off cho Anti-Reload trong Settings (theo pattern `SettingsDataStore`/`SettingsItem` đã có) sau khi đọc đầy đủ 2 file đó; mở rộng danh sách domain nếu Facebook đổi domain khu vực.

## 11. Ngăn lỗi tái diễn (Prevent recurrence)
Cờ chống double-patch; đặt tên file mới khác hoàn toàn 12 file JS hiện có; không đổi nội dung `create-release.yml` — tránh phá quy trình release ổn định.

## 12. Báo cáo tối ưu (Optimization report)
Thêm ~5KB JS + ~1.5KB Kotlin vào APK — không đáng kể so với APK <2MB của Nobook. Không thêm dependency Gradle mới, không thêm permission mới.

## 13. Đánh giá cuối cùng (Final assessment)
PR này AN TOÀN để merge ở dạng "chỉ thêm file mới" — không sửa file nào đang chạy nên không có rủi ro regression. Phần cần con người xác nhận là đoạn merge tay 6 dòng vào `NobookWV.kt` (mục 9), vì công cụ không đọc được nội dung chính xác hiện tại của file này trong phiên làm việc này. Khuyến nghị: merge PR → tự thêm đoạn đó → mở PR nhỏ thứ 2 → chờ CI xanh → tag `v1.0.0`.
