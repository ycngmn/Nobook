package com.ycngmn.nobook

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nobook_prefs")

class NobookDataStore(private val context: Context) {
    private companion object {
        val REMOVE_ADS = booleanPreferencesKey("remove_ads")
        val HIDE_SUGGESTED = booleanPreferencesKey("hide_suggestion")
        val PINCH_TO_ZOOM = booleanPreferencesKey("pinch_to_zoom")
    }

    val removeAds = context.dataStore.data.map { it[REMOVE_ADS] != false }
    suspend fun setRemoveAds(removeAds: Boolean) {
        context.dataStore.edit { it[REMOVE_ADS] = removeAds }
    }

    val hideSuggested = context.dataStore.data.map { it[HIDE_SUGGESTED] == true }
    suspend fun setHideSuggested(hideSuggestion: Boolean) {
        context.dataStore.edit { it[HIDE_SUGGESTED] = hideSuggestion }
    }

    val pinchToZoom = context.dataStore.data.map { it[PINCH_TO_ZOOM] == true }
    suspend fun setPinchToZoom(pinchToZoom: Boolean) {
        context.dataStore.edit { it[PINCH_TO_ZOOM] = pinchToZoom }
    }
}