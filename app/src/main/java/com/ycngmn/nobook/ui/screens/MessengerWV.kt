package com.ycngmn.nobook.ui.screens

import androidx.compose.runtime.Composable
import com.ycngmn.nobook.R

@Composable
fun MessengerWebView(onNavigateFB: () -> Unit) {
    BaseWebView(
        url = "https://www.facebook.com/messages",
        userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36",
        onInterceptAction = onNavigateFB,
        onPostLoad = { navigator, context ->
            navigator.evaluateJavaScript(
                context.resources.openRawResource(R.raw.messenger_scripts)
                    .bufferedReader().use { it.readText() }
            )
        }
    )
}