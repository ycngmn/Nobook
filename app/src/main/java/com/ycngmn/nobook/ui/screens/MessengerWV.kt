package com.ycngmn.nobook.ui.screens

import androidx.compose.runtime.Composable
import com.ycngmn.nobook.ui.NobookViewModel

@Composable
fun MessengerWebView(onNavigateFB: () -> Unit, viewModel: NobookViewModel) {
    BaseWebView(
        url = "https://www.facebook.com/messages",
        userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36",
        onInterceptAction = onNavigateFB,
        viewModel = viewModel
    )
}