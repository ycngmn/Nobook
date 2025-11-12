package com.ycngmn.nobook

import android.app.Application
import android.content.res.Resources
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ycngmn.nobook.utils.Script
import com.ycngmn.nobook.utils.fetchScripts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class NobookViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = NobookDataStore(application)

    private val _scripts = MutableStateFlow<String?>(null)
    val scripts = _scripts.asStateFlow()

    private val _themeColor = MutableStateFlow(Color.Transparent)
    val themeColor = _themeColor.asStateFlow()

    private val _removeAds = MutableStateFlow(true)
    val removeAds = _removeAds.asStateFlow()

    private val _enableDownloadContent = MutableStateFlow(true)
    val enableDownloadContent = _enableDownloadContent.asStateFlow()

    private val _desktopLayout = MutableStateFlow(false)
    val desktopLayout = _desktopLayout.asStateFlow()

    private val _immersiveMode = MutableStateFlow(false)
    val immersiveMode = _immersiveMode.asStateFlow()

    private val _stickyNavbar = MutableStateFlow(false)
    val stickyNavbar = _stickyNavbar.asStateFlow()

    private val _pinchToZoom = MutableStateFlow(false)
    val pinchToZoom = _pinchToZoom.asStateFlow()

    private val _amoledBlack = MutableStateFlow(false)
    val amoledBlack = _amoledBlack.asStateFlow()

    private val _hideSuggested = MutableStateFlow(false)
    val hideSuggested = _hideSuggested.asStateFlow()

    private val _hideReels = MutableStateFlow(false)
    val hideReels = _hideReels.asStateFlow()

    private val _hideStories = MutableStateFlow(false)
    val hideStories = _hideStories.asStateFlow()

    private val _hidePeopleYouMayKnow = MutableStateFlow(false)
    val hidePeopleYouMayKnow = _hidePeopleYouMayKnow.asStateFlow()

    private val _hideGroups = MutableStateFlow(false)
    val hideGroups = _hideGroups.asStateFlow()

    private val _isRevertDesktop = mutableStateOf(false)
    val isRevertDesktop = _isRevertDesktop

    init {
        runBlocking {
            _removeAds.value = dataStore.removeAds.first()
            _enableDownloadContent.value = dataStore.enableDownloadContent.first()
            _desktopLayout.value = dataStore.desktopLayout.first()
            _immersiveMode.value = dataStore.immersiveMode.first()
            _stickyNavbar.value = dataStore.stickyNavbar.first()
            _pinchToZoom.value = dataStore.pinchToZoom.first()
            _amoledBlack.value = dataStore.amoledBlack.first()
            _hideSuggested.value = dataStore.hideSuggested.first()
            _hideReels.value = dataStore.hideReels.first()
            _hideStories.value = dataStore.hideStories.first()
            _hidePeopleYouMayKnow.value = dataStore.hidePeopleYouMayKnow.first()
            _hideGroups.value = dataStore.hideGroups.first()

            _isRevertDesktop.value = dataStore.revertDesktop.first()
        }
    }

    fun setThemeColor(color: Color) { _themeColor.value = color }
    fun setScripts(script: String?) { _scripts.value = script }

    fun loadScripts(resources: Resources) {
        val scripts = listOf(
            Script(true, R.raw.scripts, "/scripts.js"), // always apply
            Script(removeAds.value, R.raw.adblock, "/adblock.js"),
            Script(enableDownloadContent.value, R.raw.download_content, "download_content.js"),
            Script(stickyNavbar.value, R.raw.sticky_navbar, "sticky_navbar.js"),
            Script(!pinchToZoom.value, R.raw.pinch_to_zoom, "pinch_to_zoom.js"),
            Script(amoledBlack.value, R.raw.amoled_black, "amoled_black.js"),
            Script(hideSuggested.value, R.raw.hide_suggested, "hide_suggested.js"),
            Script(hideReels.value, R.raw.hide_reels, "hide_reels.js"),
            Script(hideStories.value, R.raw.hide_stories, "hide_stories.js"),
            Script(hidePeopleYouMayKnow.value, R.raw.hide_pymk, "hide_pymk.js"),
            Script(hideGroups.value, R.raw.hide_groups, "hide_groups.js")
        )

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                setScripts(
                    fetchScripts(scripts) { resId ->
                        resources.openRawResource(resId)
                            .bufferedReader()
                            .use { it.readText() }
                    }
                )
            }
        }
    }

    fun setRemoveAds(removeAds: Boolean) {
        viewModelScope.launch {
            dataStore.setRemoveAds(removeAds)
        }
        _removeAds.value = removeAds
    }

    fun setEnableDownloadContent(enableDownloadContent: Boolean) {
        viewModelScope.launch {
            dataStore.setEnableDownloadContent(enableDownloadContent)
        }
        _enableDownloadContent.value = enableDownloadContent
    }

    fun setDesktopLayout(desktopLayout: Boolean) {
        viewModelScope.launch {
            dataStore.setDesktopLayout(desktopLayout)
        }
        _desktopLayout.value = desktopLayout
    }

    fun setImmersiveMode(immersiveMode: Boolean) {
        viewModelScope.launch {
            dataStore.setImmersiveMode(immersiveMode)
        }
        _immersiveMode.value = immersiveMode
    }

    fun setStickyNavbar(stickyNavbar: Boolean) {
        viewModelScope.launch {
            dataStore.setStickyNavbar(stickyNavbar)
        }
        _stickyNavbar.value = stickyNavbar
    }

    fun setPinchToZoom(pinchToZoom: Boolean) {
        viewModelScope.launch {
            dataStore.setPinchToZoom(pinchToZoom)
        }
        _pinchToZoom.value = pinchToZoom
    }

    fun setAmoledBlack(amoledBlack: Boolean) {
        viewModelScope.launch {
            dataStore.setAmoledBlack(amoledBlack)
        }
        _amoledBlack.value = amoledBlack
    }

    fun setHideSuggested(hideSuggested: Boolean) {
        viewModelScope.launch {
            dataStore.setHideSuggested(hideSuggested)
        }
        _hideSuggested.value = hideSuggested
    }

    fun setHideReels(hideReels: Boolean) {
        viewModelScope.launch {
            dataStore.setHideReels(hideReels)
        }
        _hideReels.value = hideReels
    }

    fun setHideStories(hideStories: Boolean) {
        viewModelScope.launch {
            dataStore.setHideStories(hideStories)
        }
        _hideStories.value = hideStories
    }

    fun setHidePeopleYouMayKnow(hidePeopleYouMayKnow: Boolean) {
        viewModelScope.launch {
            dataStore.setHidePeopleYouMayKnow(hidePeopleYouMayKnow)
        }
        _hidePeopleYouMayKnow.value = hidePeopleYouMayKnow
    }

    fun setHideGroups(hideGroups: Boolean) {
        viewModelScope.launch {
            dataStore.setHideGroups(hideGroups)
        }
        _hideGroups.value = hideGroups
    }


    fun setRevertDesktop(revertDesktop: Boolean) {
        viewModelScope.launch {
            dataStore.setRevertDesktop(revertDesktop)
        }
        _isRevertDesktop.value = revertDesktop
    }
}