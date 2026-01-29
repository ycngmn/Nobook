package com.ycngmn.nobook.ui.viewmodel

import android.content.res.Resources
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ycngmn.nobook.R
import com.ycngmn.nobook.data.local.entity.NobookConfig
import com.ycngmn.nobook.utils.Script
import com.ycngmn.nobook.utils.fetchScripts
import kotlinx.coroutines.launch


class MainViewModel(
    resources: Resources,
    config: NobookConfig
): ViewModel() {

    private val _themeColor = mutableStateOf(Color.Transparent)
    val themeColor: State<Color> = _themeColor

    private val _scripts = mutableStateOf<String?>(null)
    val scripts: State<String?> = _scripts

    init {
        loadScripts(
            resources,
            config
        )
    }

    fun setThemeColor(color: Color) {
        _themeColor.value = color
    }

    private fun loadScripts(
        resources: Resources,
        config: NobookConfig
    ) {
        val scripts = listOf(
            Script(true, R.raw.scripts, "scripts.js"), // always apply
            Script(config.removeAds, R.raw.adblock, "adblock.js"),
            Script(config.enableDownloadContent, R.raw.download_content, "download_content.js"),
            Script(config.enableCopyToClipboard, R.raw.copy_to_clipboard, "copy_to_clipboard.js"),
            Script(config.stickyNavbar, R.raw.sticky_navbar, "sticky_navbar.js"),
            Script(!config.pinchToZoom, R.raw.pinch_to_zoom, "pinch_to_zoom.js"),
            Script(config.amoledBlack, R.raw.amoled_black, "amoled_black.js"),
            Script(config.hideSuggested, R.raw.hide_suggested, "hide_suggested.js"),
            Script(config.hideReels, R.raw.hide_reels, "hide_reels.js"),
            Script(config.hideStories, R.raw.hide_stories, "hide_stories.js"),
            Script(config.hidePeopleYouMayKnow, R.raw.hide_pymk, "hide_pymk.js"),
            Script(config.hideGroups, R.raw.hide_groups, "hide_groups.js")
        )

        viewModelScope.launch {
            _scripts.value =
                fetchScripts(scripts) { resId ->
                    resources.openRawResource(resId)
                        .bufferedReader()
                        .use { it.readText() }
                }
        }
    }

    fun refresh(
        resources: Resources,
        config: NobookConfig
    ) {
        loadScripts(resources, config)
    }

    fun clearScripts() {
        _scripts.value = null
    }
}