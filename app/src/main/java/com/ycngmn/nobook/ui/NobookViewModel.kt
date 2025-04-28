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

    private val _hideSuggested = MutableStateFlow(false)
    val hideSuggested = _hideSuggested.asStateFlow()

    private val _pinchToZoom = MutableStateFlow(false)
    val pinchToZoom = _pinchToZoom.asStateFlow()

    init {
        runBlocking {
            _removeAds.value = dataStore.removeAds.first()
            _hideSuggested.value = dataStore.hideSuggested.first()
            _pinchToZoom.value = dataStore.pinchToZoom.first()
        }
    }

    fun setRemoveAds(removeAds: Boolean) {
        viewModelScope.launch {
            dataStore.setRemoveAds(removeAds)
        }
        _removeAds.value = removeAds
    }

    fun setHideSuggested(hideSuggested: Boolean) {
        viewModelScope.launch {
            dataStore.setHideSuggested(hideSuggested)
        }
        _hideSuggested.value = hideSuggested
    }

    fun setPinchToZoom(pinchToZoom: Boolean) {
        viewModelScope.launch {
            dataStore.setPinchToZoom(pinchToZoom)
        }
        _pinchToZoom.value = pinchToZoom
    }
}