package com.ycngmn.nobook.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.ycngmn.nobook.R
import com.ycngmn.nobook.ui.NobookViewModel
import com.ycngmn.nobook.utils.Script
import com.ycngmn.nobook.utils.fetchScripts
import com.ycngmn.nobook.utils.isAutoDesktop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun FacebookWebView(
    url: String,
    onRestart: () -> Unit,
    onOpenMessenger: () -> Unit,
    viewModel: NobookViewModel
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDesktop = viewModel.desktopLayout.collectAsState()
    val isAutoDesktop = isAutoDesktop()

    LaunchedEffect(Unit) {
        if (isAutoDesktop && !isDesktop.value) {
            viewModel.setRevertDesktop(true)
            viewModel.setDesktopLayout(true)
        }
        else if (!isAutoDesktop && viewModel.isRevertDesktop.value) {
            viewModel.setRevertDesktop(false)
            viewModel.setDesktopLayout(false)
        }
    }

    BaseWebView(
        url = url,
        userAgent = if (isDesktop.value) DESKTOP_USER_AGENT else null,
        onInterceptAction = onOpenMessenger,
        onRestart = onRestart,
        viewModel = viewModel,
        onPostLoad = {
            val cdnBase = "https://raw.githubusercontent.com/ycngmn/Nobook/refs/heads/main/app/src/main/res/raw"

            val scripts = listOf(
                Script(true, R.raw.scripts, "$cdnBase/scripts.js"), // always apply
                Script(viewModel.removeAds.value, R.raw.adblock, "$cdnBase/adblock.js"),
                Script(viewModel.enableDownloadContent.value, R.raw.download_content, "$cdnBase/download_content.js"),
                Script(viewModel.stickyNavbar.value, R.raw.sticky_navbar, "$cdnBase/sticky_navbar.js"),
                Script(!viewModel.pinchToZoom.value, R.raw.pinch_to_zoom, "$cdnBase/pinch_to_zoom.js"),
                Script(viewModel.amoledBlack.value, R.raw.amoled_black, "$cdnBase/amoled_black.js"),
                Script(viewModel.hideSuggested.value, R.raw.hide_suggested, "$cdnBase/hide_suggested.js"),
                Script(viewModel.hideReels.value, R.raw.hide_reels, "$cdnBase/hide_reels.js"),
                Script(viewModel.hideStories.value, R.raw.hide_stories, "$cdnBase/hide_stories.js"),
                Script(viewModel.hidePeopleYouMayKnow.value, R.raw.hide_pymk, "$cdnBase/hide_pymk.js"),
                Script(viewModel.hideGroups.value, R.raw.hide_groups, "$cdnBase/hide_groups.js"),
                Script(!viewModel.desktopLayout.value, R.raw.messenger_scripts, "$cdnBase/messenger_scripts.js")
            )

            scope.launch {
                withContext(Dispatchers.IO) {
                    viewModel.scripts.value = fetchScripts(scripts, context)
                }
            }
        }
    )
}

