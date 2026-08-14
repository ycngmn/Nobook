package com.ycngmn.nobook.utils.jsBridge

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

/**
 * Holds a callback the Composable registers so this background-thread JS
 * bridge can request the SAF folder picker be launched on the main thread.
 */
object DownloadFolderPicker {
    var onPickRequested: (() -> Unit)? = null
}

class DownloadFolderBridge(private val context: Context) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun pickFolder() {
        mainHandler.post {
            DownloadFolderPicker.onPickRequested?.invoke()
        }
    }
}
