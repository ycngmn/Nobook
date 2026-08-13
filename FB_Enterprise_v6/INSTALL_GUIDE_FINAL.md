# FB Enterprise AdBlocker v8.0 — Hướng Dẫn Tích Hợp FINAL

> **Phiên bản:** v8.0 Enterprise FINAL  
> **Mục tiêu:** Tích hợp Smali mod vào source Facebook Revance APK  
> **Cập nhật:** 2026-03-31  

---

## MỤC LỤC

1. [Yêu cầu hệ thống](#1-yêu-cầu-hệ-thống)
2. [Cấu trúc thư mục dự án](#2-cấu-trúc-thư-mục-dự-án)
3. [Tích hợp trên MÁY TÍNH (Windows/Linux/macOS)](#3-tích-hợp-trên-máy-tính)
4. [Tích hợp trên ĐIỆN THOẠI (Android)](#4-tích-hợp-trên-điện-thoại)
5. [Giải thích PATCH_TARGETS_FINAL.csv](#5-giải-thích-patch_targets_finalcsv)
6. [Xử lý lỗi thường gặp](#6-xử-lý-lỗi-thường-gặp)
7. [Lưu ý bảo mật & pháp lý](#7-lưu-ý-bảo-mật--pháp-lý)

---

## 1. Yêu Cầu Hệ Thống

### Công cụ bắt buộc

| Công cụ | Phiên bản | Link |
|---|---|---|
| **Java JDK** | 11 hoặc 17 | https://adoptium.net |
| **apktool** | ≥ 2.9.0 | https://apktool.org |
| **zipalign** | Android SDK Build-Tools 34+ | Android Studio SDK Manager |
| **apksigner** | Android SDK Build-Tools 34+ | Android Studio SDK Manager |
| **keytool** | Đi kèm JDK | — |

### Tùy chọn (nâng cao)

| Công cụ | Mục đích |
|---|---|
| **uber-apk-signer** | Sign nhiều APK một lúc |
| **MT Manager / NP Manager** | Patch trực tiếp trên Android |
| **jadx** | Đọc decompiled Java để kiểm tra patch |

### Facebook APK Nguồn
- Tải Facebook APK gốc từ: [apkmirror.com](https://apkmirror.com) (chọn đúng kiến trúc: arm64-v8a)
- Khuyến nghị dùng **Facebook Revance** base APK (đã strip signature verification)

---

## 2. Cấu Trúc Thư Mục Dự Án

```
FB_Enterprise_v6/
├── build.bat                          ← Script build tự động (Windows)
├── smali/
│   └── com/enterprise/mod/
│       ├── L1_AdBlockEngine.smali     v8.0 – Core engine, URL pattern match
│       ├── L2L3L4_AdKill.smali        v7.0 – DOM kill + OkHttp + GraphQL filter
│       ├── L5_UrlGuard.smali          v7.0 – Tracking param stripper
│       ├── L6_NetworkGuard.smali      v7.0 – DNS/host block + SSL bypass
│       ├── L7_Watchdog.smali          v8.0 – Auto-reinject watchdog (30s)
│       ├── L8_VideoDownloader.smali   v8.0 – Video URL capture + download
│       ├── L9_FeedFilter.smali        v8.0 – Feed/Stories ad filter
│       ├── L10_UiExtras.smali         v8.0 – Dark mode, UI tweaks
│       ├── L11_UpdateBlocker.smali    v8.0 – Block in-app update check
│       ├── L12_CookieExporter.smali   v9.0 – Export session cookies
│       ├── L13_VirtualCamera.smali    v8.0 – Virtual camera injection
│       ├── L13_VirtualCamera$ErrorListener.smali
│       ├── L13_VirtualCamera$PreparedListener.smali
│       ├── ModInit.smali              v8.0 – Bootstrap all layers
│       ├── ModApplication.smali       v8.0 – Replaces KatanaApplication
│       ├── ModInit$WebViewClientHook.smali
│       ├── ModInit$CookieJsInterface.smali
│       └── MModInit$VideoJsInterface.smali
├── assets/
│   └── guard_v9.2_Enterprise_FINAL.js ← WebView JS guard
├── mod_enterprise.jks                 ← Tự tạo khi build lần đầu
└── PATCH_TARGETS_FINAL.csv            ← Danh sách patch targets
```

---

## 3. Tích Hợp Trên Máy Tính

### 3.1 Chuẩn bị môi trường (Windows)

```bat
:: Kiểm tra Java
java -version

:: Kiểm tra apktool
apktool --version

:: Kiểm tra zipalign và apksigner (đường dẫn thường gặp)
:: C:\Users\<user>\AppData\Local\Android\Sdk\build-tools\34.0.0\
where zipalign
where apksigner
```

> **Tip:** Thêm `C:\Users\<user>\AppData\Local\Android\Sdk\build-tools\34.0.0\` vào biến môi trường PATH.

### 3.2 Chạy build.bat (Tự động – Khuyến nghị)

```bat
:: Cú pháp:
build.bat <đường_dẫn_apk_gốc> [thư_mục_output]

:: Ví dụ:
build.bat C:\APKs\facebook_v460.apk C:\APKs\output

:: Kết quả:
:: output\FB_Enterprise_v8.0_FINAL.apk
```

**build.bat sẽ tự động:**
1. Tạo keystore `mod_enterprise.jks` nếu chưa có
2. Decompile APK bằng apktool
3. Copy toàn bộ smali files vào `smali/com/enterprise/mod/`
4. Copy `guard_v9.2_Enterprise_FINAL.js` vào `assets/`
5. Patch `AndroidManifest.xml` — thay `KatanaApplication` → `ModApplication`
6. Rebuild APK
7. Zipalign + Sign bằng keystore

---

### 3.3 Build thủ công (Linux/macOS)

#### Bước 1 – Decompile APK

```bash
apktool d -f -o ./fb_decompiled facebook.apk
```

#### Bước 2 – Inject smali files

```bash
# Tạo thư mục đích nếu chưa có
mkdir -p ./fb_decompiled/smali/com/enterprise/mod

# Copy toàn bộ smali
cp -r ./smali/com/enterprise/mod/* ./fb_decompiled/smali/com/enterprise/mod/

# Copy assets
cp ./assets/guard_v9.2_Enterprise_FINAL.js ./fb_decompiled/assets/
```

#### Bước 3 – Patch AndroidManifest.xml

```bash
# Thay thế KatanaApplication bằng ModApplication
sed -i 's/android:name="com\.facebook\.katana\.KatanaApplication"/android:name="com.enterprise.mod.ModApplication"/g' \
    ./fb_decompiled/AndroidManifest.xml

# Xác nhận patch
grep -n "ModApplication" ./fb_decompiled/AndroidManifest.xml
```

#### Bước 4 – Patch smali_classes nếu cần

> Một số phiên bản Facebook dùng `smali_classes2` hoặc `smali_classes3`.  
> Kiểm tra bằng lệnh:

```bash
ls ./fb_decompiled/ | grep smali
# Nếu có smali_classes2, copy vào đó:
mkdir -p ./fb_decompiled/smali_classes2/com/enterprise/mod
cp -r ./smali/com/enterprise/mod/* ./fb_decompiled/smali_classes2/com/enterprise/mod/
```

#### Bước 5 – Rebuild APK

```bash
apktool b -o ./fb_unsigned.apk ./fb_decompiled
```

#### Bước 6 – Tạo keystore (lần đầu)

```bash
keytool -genkeypair -v \
  -keystore mod_enterprise.jks \
  -alias enterprise_key \
  -keyalg RSA -keysize 2048 \
  -validity 10000 \
  -storepass enterprise2024 \
  -keypass enterprise2024 \
  -dname "CN=Enterprise Mod, OU=FB Mod, O=Enterprise, L=SG, ST=SG, C=SG"
```

#### Bước 7 – Zipalign

```bash
zipalign -v -p 4 ./fb_unsigned.apk ./fb_aligned.apk
```

#### Bước 8 – Sign APK

```bash
apksigner sign \
  --ks mod_enterprise.jks \
  --ks-key-alias enterprise_key \
  --ks-pass pass:enterprise2024 \
  --key-pass pass:enterprise2024 \
  --out ./FB_Enterprise_v8.0_FINAL.apk \
  ./fb_aligned.apk

# Xác nhận signature
apksigner verify --verbose ./FB_Enterprise_v8.0_FINAL.apk
```

#### Bước 9 – Cài lên điện thoại

```bash
adb install -r ./FB_Enterprise_v8.0_FINAL.apk
```

---

### 3.4 Kiểm tra patch thành công

```bash
# Mở decompiled và tìm ModApplication
grep -r "ModApplication" ./fb_decompiled/AndroidManifest.xml

# Kiểm tra smali đã inject
ls -la ./fb_decompiled/smali/com/enterprise/mod/

# Kiểm tra assets
ls -la ./fb_decompiled/assets/ | grep guard
```

---

## 4. Tích Hợp Trên Điện Thoại

> **Yêu cầu:** Android 10+ | Root không bắt buộc nếu dùng MT Manager  
> **Khuyến nghị:** MT Manager Pro hoặc NP Manager

---

### 4.1 Dùng MT Manager (Không cần root – Phổ biến nhất)

#### Bước 1 – Chuẩn bị file

Chuyển các file vào thư mục nội bộ điện thoại:
```
/sdcard/FB_Enterprise_v6/
├── smali/com/enterprise/mod/*.smali
├── assets/guard_v9.2_Enterprise_FINAL.js
└── PATCH_TARGETS_FINAL.csv
```

#### Bước 2 – Mở APK gốc trong MT Manager

1. Mở **MT Manager** → Điều hướng đến thư mục chứa `facebook.apk`
2. Nhấn giữ file APK → chọn **"Open with APK Editor"**

#### Bước 3 – Inject smali files

1. Trong APK Editor → mở thư mục `smali/` (hoặc `smali_classes2/`)
2. Điều hướng đến `com/facebook/` (để tham khảo cấu trúc)
3. **Tạo thư mục mới:** `com/enterprise/mod/`
4. Copy tất cả `.smali` files từ `/sdcard/FB_Enterprise_v6/smali/com/enterprise/mod/` vào đây

#### Bước 4 – Inject guard JS

1. Trong APK Editor → mở thư mục `assets/`
2. **Import file:** `guard_v9.2_Enterprise_FINAL.js`

#### Bước 5 – Patch AndroidManifest.xml

1. Trong APK Editor → nhấn vào `AndroidManifest.xml`
2. Chọn **"Text Edit"**
3. Tìm dòng:
   ```xml
   android:name="com.facebook.katana.KatanaApplication"
   ```
4. Thay thành:
   ```xml
   android:name="com.enterprise.mod.ModApplication"
   ```
5. Lưu file

#### Bước 6 – Sign và cài đặt

1. Nhấn nút **"Save"** → MT Manager tự rebuild + sign
2. Chọn **"Install"** khi được hỏi
3. Gỡ bản Facebook gốc trước nếu có (hoặc dùng tên package khác)

---

### 4.2 Dùng NP Manager (Android 11+)

Quy trình tương tự MT Manager:
1. Mở NP Manager → **Import APK**
2. **Smali Edit** → tạo folder `com/enterprise/mod/` → import smali files
3. **Asset Edit** → import `guard_v9.2_Enterprise_FINAL.js`
4. **Manifest Edit** → thay `KatanaApplication` → `ModApplication`
5. **Build + Sign** → cài đặt

---

### 4.3 Dùng Termux + apktool (Android với Root hoặc UserSpace)

```bash
# Cài apktool trong Termux
pkg update && pkg install apktool openjdk-17

# Decompile
apktool d -f -o ~/fb_decompiled ~/facebook.apk

# Inject files
mkdir -p ~/fb_decompiled/smali/com/enterprise/mod
cp ~/FB_Enterprise_v6/smali/com/enterprise/mod/* ~/fb_decompiled/smali/com/enterprise/mod/
cp ~/FB_Enterprise_v6/assets/guard_v9.2_Enterprise_FINAL.js ~/fb_decompiled/assets/

# Patch manifest
sed -i 's/com\.facebook\.katana\.KatanaApplication/com.enterprise.mod.ModApplication/g' \
    ~/fb_decompiled/AndroidManifest.xml

# Rebuild
apktool b -o ~/fb_unsigned.apk ~/fb_decompiled

# Sign bằng uber-apk-signer
java -jar uber-apk-signer.jar --apks ~/fb_unsigned.apk

# Cài đặt
adb install -r ~/fb_unsigned-aligned-debugSigned.apk
```

---

### 4.4 Kiểm tra hoạt động trên thiết bị

Sau khi cài đặt, mở Facebook và kiểm tra:

1. **Logcat** (qua adb hoặc MatLog):
   ```
   adb logcat | grep -E "ModInit|L1_AdBlock|FBGuard"
   ```
   Bạn sẽ thấy:
   ```
   I ModApplication: FB Enterprise AdBlocker v8.0 – ModApplication started
   I ModInit_v8.0  : FB Enterprise AdBlocker v8.0 – All layers initialized OK
   I L7_Watchdog   : Watchdog started. Interval: 30000ms
   ```

2. **Kiểm tra JS Guard** trong WebView DevTools:
   ```javascript
   window.__fbGuardStats()
   // → {"blocked":12,"removed":5,"intercepted":3}
   ```

3. **Test export cookie:**
   ```javascript
   window.__fbGuardExportCookies()
   // → "Export triggered via L12_CookieExporter"
   ```

---

## 5. Giải Thích PATCH_TARGETS_FINAL.csv

File `PATCH_TARGETS_FINAL.csv` liệt kê tất cả các điểm hook:

| Cột | Ý nghĩa |
|---|---|
| `Layer` | Layer số (L1–L13, ModInit) |
| `Target_Class` | Class Facebook cần hook |
| `Target_Method` | Method cần patch |
| `Patch_Type` | HOOK_REPLACE / HOOK_APPEND / HOOK_WRAP / MANIFEST_REPLACE |
| `Smali_File` | File smali thực hiện patch |
| `Description` | Mô tả chức năng |
| `Version` | Phiên bản layer |
| `Status` | ACTIVE / DISABLED |

### Các kiểu patch:

| Patch_Type | Ý nghĩa |
|---|---|
| `HOOK_REPLACE` | Thay thế hoàn toàn method gốc |
| `HOOK_APPEND` | Thêm code vào cuối method gốc (sau `invoke-super`) |
| `HOOK_WRAP` | Bao method gốc trong try/catch hoặc điều kiện |
| `MANIFEST_REPLACE` | Sửa AndroidManifest.xml, không patch smali |

---

## 6. Xử Lý Lỗi Thường Gặp

### ❌ apktool: brut.androlib.AndrolibException

```
Nguyên nhân: APK dùng resource obfuscation hoặc split APK
Giải pháp  : apktool d -f --only-main-classes -o ./fb_decompiled facebook.apk
```

### ❌ smali: duplicate class definition

```
Nguyên nhân: Class đã tồn tại trong smali_classes2 hoặc smali_classes3
Giải pháp  : Tìm và xóa bản cũ, chỉ giữ 1 bản trong 1 smali folder
```

### ❌ INSTALL_FAILED_UPDATE_INCOMPATIBLE

```
Nguyên nhân: Signature mới không khớp với bản đã cài
Giải pháp  : adb uninstall com.facebook.katana
             Sau đó cài lại: adb install -r FB_Enterprise_v8.0_FINAL.apk
```

### ❌ App crash ngay khi mở (ClassNotFoundException: ModApplication)

```
Nguyên nhân: Smali files chưa vào đúng smali folder (smali vs smali_classes2)
Giải pháp  :
  1. Kiểm tra folder: ls fb_decompiled/ | grep smali
  2. Copy smali vào TẤT CẢ smali folders
  3. Rebuild lại
```

### ❌ guard_v9.2 không chạy (không thấy log FBGuard)

```
Nguyên nhân: File JS chưa được inject đúng path, hoặc WebViewClientHook chưa active
Giải pháp  :
  1. Kiểm tra: ls fb_decompiled/assets/ | grep guard
  2. Kiểm tra ModInit$WebViewClientHook.smali đã được inject
  3. Xem logcat: adb logcat | grep ModInit
```

### ❌ Build thất bại: zipalign not found

```
Giải pháp  :
  Windows: thêm PATH → C:\Users\<user>\AppData\Local\Android\Sdk\build-tools\34.0.0
  Linux  : export PATH=$PATH:$ANDROID_HOME/build-tools/34.0.0
  macOS  : export PATH=$PATH:~/Library/Android/sdk/build-tools/34.0.0
```

---

## 7. Lưu Ý Bảo Mật & Pháp Lý

> ⚠️ **Dành cho mục đích nghiên cứu và sử dụng cá nhân.**  
> Việc mod APK và phân phối lại có thể vi phạm ToS của Facebook và luật bản quyền tại một số quốc gia.  
> Chỉ sử dụng trên thiết bị và tài khoản của bạn.  
> Tác giả không chịu trách nhiệm về bất kỳ thiệt hại hoặc vi phạm nào phát sinh từ việc sử dụng mod này.

---

*FB Enterprise AdBlocker v8.0 — Build Date: 2026-03-31*
