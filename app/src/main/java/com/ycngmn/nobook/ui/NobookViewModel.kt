package com.ycngmn.nobook.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ycngmn.nobook.NobookDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class NobookViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = NobookDataStore(application)

    private val _removeAds = MutableStateFlow(true)
    val removeAds = _removeAds.asStateFlow()

    private val _enableDownloadContent = MutableStateFlow(true)
    val enableDownloadContent = _enableDownloadContent.asStateFlow()

    private val _pinchToZoom = MutableStateFlow(false)
    val pinchToZoom = _pinchToZoom.asStateFlow()

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

    init {
        runBlocking {
            _removeAds.value = dataStore.removeAds.first()
            _enableDownloadContent.value = dataStore.enableDownloadContent.first()
            _pinchToZoom.value = dataStore.pinchToZoom.first()
            _hideSuggested.value = dataStore.hideSuggested.first()
            _hideReels.value = dataStore.hideReels.first()
            _hideStories.value = dataStore.hideStories.first()
            _hidePeopleYouMayKnow.value = dataStore.hidePeopleYouMayKnow.first()
            _hideGroups.value = dataStore.hideGroups.first()
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

    fun setPinchToZoom(pinchToZoom: Boolean) {
        viewModelScope.launch {
            dataStore.setPinchToZoom(pinchToZoom)
        }
        _pinchToZoom.value = pinchToZoom
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
}