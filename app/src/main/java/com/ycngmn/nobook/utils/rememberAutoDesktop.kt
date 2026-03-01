package com.ycngmn.nobook.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun rememberAutoDesktop(): Boolean {
    val configuration = LocalConfiguration.current
    return remember {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            true
        } else {
            configuration.smallestScreenWidthDp >= 600
        }
    }
}