package com.ycngmn.nobook.utils

import android.os.Handler
import android.os.Looper
import android.view.Window
import android.webkit.JavascriptInterface
import androidx.core.graphics.toColorInt
import com.ycngmn.nobook.utils.styling.setStatusBarColor

class ThemeChangeInterface(
    private val window: Window
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onThemeColorChanged(newColor: String?) {
        if (newColor.isNullOrBlank()) return

        try {
            val colorInt = newColor.toColorInt()
            mainHandler.post {
                setStatusBarColor(window, colorInt)
            }
        } catch (_: Exception) {}
    }
}