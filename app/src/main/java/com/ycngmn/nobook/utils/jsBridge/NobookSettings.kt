package com.ycngmn.nobook.utils.jsBridge

import android.webkit.JavascriptInterface

class NobookSettings (
    private val toggleSettings: () -> Unit,
) {
    @JavascriptInterface
    fun onSettingsToggle() = toggleSettings()
}