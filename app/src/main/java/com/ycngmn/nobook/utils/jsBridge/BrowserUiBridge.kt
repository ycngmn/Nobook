package com.ycngmn.nobook.utils.jsBridge

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

class BrowserUiBridge(
    private val onBottomBarVisibilityChanged: (Boolean) -> Unit,
) {
    private val mainHandler =
        Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun setBottomBarVisible(visible: Boolean) {
        mainHandler.post {
            onBottomBarVisibilityChanged(visible)
        }
    }
}