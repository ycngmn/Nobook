package com.ycngmn.nobook.ui.screens

import androidx.compose.runtime.Composable
import com.ycngmn.nobook.R

//Displays a WebView for the Facebook mobile site
@Composable
fun FacebookWebView(onOpenMessenger: () -> Unit) {
    BaseWebView(
        url = "https://m.facebook.com/",
        onInterceptAction = onOpenMessenger ,
        onPostLoad = { navigator, context ->
            val script = context.resources.openRawResource(R.raw.scripts)
            navigator.evaluateJavaScript(
                script.bufferedReader().use { it.readText() }
            )
        }
    )
}

