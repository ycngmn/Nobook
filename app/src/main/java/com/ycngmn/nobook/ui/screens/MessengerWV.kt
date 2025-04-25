package com.ycngmn.nobook.ui.screens

import androidx.compose.runtime.Composable

//Displays a WebView for the Facebook mobile site
@Composable
fun MessengerWebView() {
    BaseWebView(
        url = "https://facebook.com/messages",
        userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36"
    )
}