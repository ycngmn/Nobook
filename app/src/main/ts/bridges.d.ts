// Ambient type declarations for the four @JavascriptInterface bridges
// registered in NobookWV.kt around lines 262-277 via addJavascriptInterface.
// Mirrors the Kotlin signatures exactly so TS-authored scripts in
// app/src/main/ts/*.ts can catch Kotlin<->JS shape drift at compile time
// (tsc --noEmit) before any live FB Playwright run.

// Mirrors com.ycngmn.nobook.utils.jsBridge.NobookSettings (`onSettingsToggle(): Unit`).
interface SettingsBridge {
  onSettingsToggle(): void;
}

// Mirrors com.ycngmn.nobook.utils.jsBridge.ThemeChange
// (`onThemeColorChanged(newColor: String?)`).
interface ThemeBridge {
  onThemeColorChanged(newColor: string | null): void;
}

// Mirrors com.ycngmn.nobook.utils.jsBridge.DownloadBridge
// (`downloadBase64File(base64Data: String, mimeType: String)`).
interface DownloadBridge {
  downloadBase64File(base64Data: string, mimeType: string): void;
}

// Mirrors com.ycngmn.nobook.utils.jsBridge.ClipboardBridge
// (`copyImageToClipboard(base64Data: String, mimeType: String)`).
interface ClipboardBridge {
  copyImageToClipboard(base64Data: string, mimeType: string): void;
}

// Global window augmentations:
//   - each bridge exposed via addJavascriptInterface() as window.<Name>.
//   - `window.backHandlerNB` returns the user's intent for the back press
//     (defined at scripts.js:120; consumed by the backHandler LaunchedEffect
//     in NobookWV.kt around line 91). The four string literals:
//       "false"     -> backHandler forwarded to WebView default navigation
//       "exit"      -> user is at top-of-feed, app should exit
//       "scrolling" -> middle-of-feed; scroll to top, don't navigate away
//       "top"       -> at scroll-top, exit gesture
//   - `window._downloadBridgeInitialized` gates the one-time DownloadBridge
//     readiness handshake (one-shot per page-finish; scripts.js around 350-357).
declare global {
  interface Window {
    SettingsBridge?: SettingsBridge;
    ThemeBridge?: ThemeBridge;
    DownloadBridge?: DownloadBridge;
    ClipboardBridge?: ClipboardBridge;
    backHandlerNB?: () => "false" | "exit" | "scrolling" | "top";
    _downloadBridgeInitialized?: boolean;
  }
}

export {};
