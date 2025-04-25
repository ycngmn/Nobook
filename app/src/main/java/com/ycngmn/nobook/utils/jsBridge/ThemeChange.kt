package com.ycngmn.nobook.utils.jsBridge

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

class ThemeChange(
    private val themeColor: MutableState<Color>
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onThemeColorChanged(newColor: String?) {
        if (newColor.isNullOrBlank()) return
        themeColor.value = Color(newColor.toColorInt())
    }
}