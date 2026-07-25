package com.ycngmn.nobook.ui.viewmodel

import android.content.res.Resources
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.ycngmn.nobook.R
import com.ycngmn.nobook.utils.Script
import com.ycngmn.nobook.utils.loadBundledScripts

class MainViewModel(
    resources: Resources,
    settings: SettingsViewModel,
) : ViewModel() {

    private val _themeColor = mutableStateOf(Color.Transparent)
    val themeColor: State<Color> = _themeColor

    private val _scripts = mutableStateOf<String?>(null)
    val scripts: State<String?> = _scripts

    init {
        loadScripts(
            resources = resources,
            settings = settings,
        )
    }

    fun setThemeColor(color: Color) {
        _themeColor.value = color
    }

    private fun loadScripts(
        resources: Resources,
        settings: SettingsViewModel,
    ) {
        val scripts = listOf(
            Script(true, R.raw.scripts),
            Script(settings.removeAds.value, R.raw.adblock),
            Script(
                settings.enableDownloadContent.value,
                R.raw.download_content,
            ),
            Script(
                settings.enableCopyToClipboard.value,
                R.raw.copy_to_clipboard,
            ),
            Script(settings.stickyNavbar.value, R.raw.sticky_navbar),
            Script(!settings.pinchToZoom.value, R.raw.pinch_to_zoom),
            Script(settings.amoledBlack.value, R.raw.amoled_black),
            Script(settings.hideSuggested.value, R.raw.hide_suggested),
            Script(settings.hideReels.value, R.raw.hide_reels),
            Script(settings.hideStories.value, R.raw.hide_stories),
            Script(
                settings.hidePeopleYouMayKnow.value,
                R.raw.hide_pymk,
            ),
            Script(settings.hideGroups.value, R.raw.hide_groups),
        )

        _scripts.value = loadBundledScripts(
            scripts = scripts,
            contentProvider = { resourceId ->
                resources
                    .openRawResource(resourceId)
                    .bufferedReader()
                    .use { reader ->
                        reader.readText()
                    }
            },
        )
    }

    fun refresh(
        resources: Resources,
        settings: SettingsViewModel,
    ) {
        clearScripts()
        loadScripts(
            resources = resources,
            settings = settings,
        )
    }

    private fun clearScripts() {
        _scripts.value = null
    }
}