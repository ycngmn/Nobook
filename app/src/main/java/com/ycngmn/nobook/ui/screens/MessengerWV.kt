package com.ycngmn.nobook.ui.screens

import androidx.compose.runtime.Composable
import com.ycngmn.nobook.R

@Composable
fun MessengerWebView() {
    BaseWebView(
        url = "https://www.messenger.com/",
        userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36",
        onPostLoad = { navigator, context ->
            navigator.evaluateJavaScript(
                context.resources.openRawResource(R.raw.set_viewport)
                    .bufferedReader().use { it.readText() }
            )
        }
    )
}