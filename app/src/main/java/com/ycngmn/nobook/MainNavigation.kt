package com.ycngmn.nobook

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ycngmn.nobook.ui.screens.NobookWebView
import com.ycngmn.nobook.ui.theme.NobookTheme

@Composable
fun MainNavigation(data: Uri?) {

    val viewModel: NobookViewModel = viewModel()
    val themeColor = viewModel.themeColor.collectAsState().value
    val isDarkTheme = remember(themeColor) {
        ColorUtils.calculateLuminance(
            themeColor.toArgb()
        ) < 0.5
    }

    NobookTheme(darkTheme = isDarkTheme) {
        NobookWebView(
            data?.toString() ?: "https://facebook.com/",
            viewModel = viewModel
        )
    }
}