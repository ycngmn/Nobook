package com.ycngmn.nobook.utils.jsBridge

import android.webkit.JavascriptInterface
import androidx.compose.runtime.MutableState

class NavigateFB(
    private val switch: MutableState<Boolean>
) {
    @JavascriptInterface
    fun onNavigateFB() {
        switch.value = true
    }
}