@echo off
:: ============================================================
:: FB Enterprise AdBlocker v8.0 – Auto Build Script
:: Requires: apktool, zipalign, apksigner (Android Build Tools)
::           Java JDK 11+, PowerShell 5+
:: Usage   : build.bat <input_apk> [output_dir]
:: ============================================================
setlocal EnableDelayedExpansion

set "VERSION=8.0"
set "KEYSTORE=mod_enterprise.jks"
set "KEY_ALIAS=enterprise_key"
set "KEY_PASS=enterprise2024"
set "STORE_PASS=enterprise2024"
set "SMALI_PKG=smali\com\enterprise\mod"
set "ASSETS_SRC=assets"

:: Validate arguments
if "%~1"=="" (
    echo [ERROR] No input APK specified.
    echo Usage: build.bat ^<facebook.apk^> [output_dir]
    exit /b 1
)
set "INPUT_APK=%~1"
set "OUTPUT_DIR=%~2"
if "%OUTPUT_DIR%"=="" set "OUTPUT_DIR=%~dp0output"

:: Create output dir
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

echo.
echo ╔══════════════════════════════════════════════════════╗
echo ║   FB Enterprise AdBlocker v%VERSION% – BUILD STARTED   ║
echo ╚══════════════════════════════════════════════════════╝
echo.

:: ─── Step 1: Generate keystore if missing ────────────────
if not exist "%KEYSTORE%" (
    echo [*] Generating keystore: %KEYSTORE%...
    keytool -genkeypair -v -keystore "%KEYSTORE%" ^
        -alias "%KEY_ALIAS%" ^
        -keyalg RSA -keysize 2048 ^
        -validity 10000 ^
        -storepass "%STORE_PASS%" ^
        -keypass "%KEY_PASS%" ^
        -dname "CN=Enterprise Mod, OU=FB Mod, O=Enterprise, L=SG, ST=SG, C=SG"
    if !ERRORLEVEL! NEQ 0 (echo [ERROR] keytool failed & exit /b 1)
    echo [+] Keystore created.
) else (
    echo [*] Keystore already exists, skipping generation.
)

:: ─── Step 2: Decompile APK ───────────────────────────────
set "WORK_DIR=%TEMP%\fb_mod_build_%RANDOM%"
echo [*] Decompiling: %INPUT_APK% → %WORK_DIR%
apktool d -f -o "%WORK_DIR%" "%INPUT_APK%"
if !ERRORLEVEL! NEQ 0 (echo [ERROR] apktool decompile failed & exit /b 1)
echo [+] Decompile complete.

:: ─── Step 3: Inject smali files ──────────────────────────
set "TARGET_SMALI=%WORK_DIR%\smali\com\enterprise\mod"
echo [*] Injecting Enterprise Mod smali files...
if not exist "%TARGET_SMALI%" mkdir "%TARGET_SMALI%"
xcopy /E /Y /Q "%SMALI_PKG%\*" "%TARGET_SMALI%\"
if !ERRORLEVEL! NEQ 0 (echo [ERROR] smali copy failed & exit /b 1)
echo [+] Smali injection complete.

:: ─── Step 4: Inject assets ───────────────────────────────
echo [*] Injecting guard_v9.2_Enterprise_FINAL.js...
xcopy /Y /Q "%ASSETS_SRC%\*" "%WORK_DIR%\assets\"
if !ERRORLEVEL! NEQ 0 (echo [ERROR] assets copy failed & exit /b 1)
echo [+] Assets injection complete.

:: ─── Step 5: Patch AndroidManifest.xml ───────────────────
echo [*] Patching AndroidManifest.xml for ModApplication...
powershell -NoProfile -Command ^
  "(Get-Content '%WORK_DIR%\AndroidManifest.xml') ^
   -replace 'android:name=\"com\.facebook\.katana\.KatanaApplication\"', ^
            'android:name=\"com.enterprise.mod.ModApplication\"' ^
   | Set-Content '%WORK_DIR%\AndroidManifest.xml'"
echo [+] Manifest patched.

:: ─── Step 6: Rebuild APK ─────────────────────────────────
set "UNSIGNED_APK=%OUTPUT_DIR%\fb_enterprise_v%VERSION%_unsigned.apk"
echo [*] Rebuilding APK...
apktool b -o "%UNSIGNED_APK%" "%WORK_DIR%"
if !ERRORLEVEL! NEQ 0 (echo [ERROR] apktool rebuild failed & exit /b 1)
echo [+] Rebuild complete: %UNSIGNED_APK%

:: ─── Step 7: Zipalign ────────────────────────────────────
set "ALIGNED_APK=%OUTPUT_DIR%\fb_enterprise_v%VERSION%_aligned.apk"
echo [*] Zipaligning...
zipalign -v -p 4 "%UNSIGNED_APK%" "%ALIGNED_APK%"
if !ERRORLEVEL! NEQ 0 (echo [ERROR] zipalign failed & exit /b 1)
echo [+] Zipalign complete.

:: ─── Step 8: Sign APK ────────────────────────────────────
set "SIGNED_APK=%OUTPUT_DIR%\FB_Enterprise_v%VERSION%_FINAL.apk"
echo [*] Signing APK...
apksigner sign ^
    --ks "%KEYSTORE%" ^
    --ks-key-alias "%KEY_ALIAS%" ^
    --ks-pass "pass:%STORE_PASS%" ^
    --key-pass "pass:%KEY_PASS%" ^
    --out "%SIGNED_APK%" ^
    "%ALIGNED_APK%"
if !ERRORLEVEL! NEQ 0 (echo [ERROR] apksigner failed & exit /b 1)
echo [+] Signed APK: %SIGNED_APK%

:: ─── Step 9: Cleanup temp ────────────────────────────────
echo [*] Cleaning up temp files...
rd /s /q "%WORK_DIR%"
del "%UNSIGNED_APK%" 2>nul
del "%ALIGNED_APK%" 2>nul

echo.
echo ╔══════════════════════════════════════════════════════╗
echo ║  BUILD SUCCESS → %SIGNED_APK%
echo ╚══════════════════════════════════════════════════════╝
echo.
endlocal
