package com.ycngmn.nobook.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ycngmn.nobook.R
import com.ycngmn.nobook.ui.NobookViewModel

@Composable
fun FacebookWebView(
    url: String,
    onRestart: () -> Unit,
    onOpenMessenger: () -> Unit
) {
    val viewModel: NobookViewModel = viewModel(key = "Nobook")
    // Nobook settings values.
    val removeAds = viewModel.removeAds
    val hideSuggested = viewModel.hideSuggested
    val pinchToZoom = viewModel.pinchToZoom

    BaseWebView(
        url = url,
        onInterceptAction = onOpenMessenger,
        onPostLoad = { navigator, context ->
            val generalScript = context.resources.openRawResource(R.raw.scripts)
            val adblockScript = context.resources.openRawResource(R.raw.adblock)
            val hideSuggestedScript = context.resources.openRawResource(R.raw.hide_suggested)
            val pinchToZoomScript = context.resources.openRawResource(R.raw.pinch_to_zoom)
            navigator.evaluateJavaScript(
                generalScript.bufferedReader().use { it.readText() }
            )

            if (removeAds.value) {
                navigator.evaluateJavaScript(
                    adblockScript.bufferedReader().use { it.readText() }
                )
            }

            if (hideSuggested.value) {
                navigator.evaluateJavaScript(
                    hideSuggestedScript.bufferedReader().use { it.readText() }
                )
            }

            if (!pinchToZoom.value) {
                navigator.evaluateJavaScript(
                    pinchToZoomScript.bufferedReader().use { it.readText() }
                )
            }
        },
        onRestart = onRestart
    )
}

