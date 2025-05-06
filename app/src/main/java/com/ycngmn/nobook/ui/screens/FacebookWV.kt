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
    val enableDownloadContent = viewModel.enableDownloadContent
    val pinchToZoom = viewModel.pinchToZoom
    val hideSuggested = viewModel.hideSuggested
    val hideReels = viewModel.hideReels
    val hideStories = viewModel.hideStories
    val hidePeopleYouMayKnow = viewModel.hidePeopleYouMayKnow
    val hideGroups = viewModel.hideGroups

    BaseWebView(
        url = url,
        onInterceptAction = onOpenMessenger,
        onPostLoad = { navigator, context ->

            data class Script(
                val condition: Boolean,
                val scriptRes: Int
            )

            val scripts = listOf(
                Script(true, R.raw.scripts), // always apply
                Script(removeAds.value, R.raw.adblock),
                Script(!pinchToZoom.value, R.raw.pinch_to_zoom),
                Script(enableDownloadContent.value, R.raw.download_content),
                Script(hideSuggested.value, R.raw.hide_suggested),
                Script(hideReels.value, R.raw.hide_reels),
                Script(hideStories.value, R.raw.hide_stories),
                Script(hidePeopleYouMayKnow.value, R.raw.hide_pymk),
                Script(hideGroups.value, R.raw.hide_groups)

            )

            scripts.filter { it.condition }.forEach { script ->
                val scriptText =
                    context.resources.openRawResource(script.scriptRes)
                    .bufferedReader().use { it.readText() }
                navigator.evaluateJavaScript(scriptText)
            }
        },
        onRestart = onRestart
    )
}

