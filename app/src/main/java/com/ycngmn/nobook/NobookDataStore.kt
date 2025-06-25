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
        val ENABLE_DOWNLOAD_CONTENT = booleanPreferencesKey("enable_download_content")
        val DESKTOP_LAYOUT = booleanPreferencesKey("desktop_layout")
        val IMMERSIVE_MODE = booleanPreferencesKey("immersive_mode")
        val STICKY_NAVBAR = booleanPreferencesKey("sticky_navbar")
        val PINCH_TO_ZOOM = booleanPreferencesKey("pinch_to_zoom")
        val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
        val HIDE_SUGGESTED = booleanPreferencesKey("hide_suggestion")
        val HIDE_REELS = booleanPreferencesKey("hide_reels")
        val HIDE_STORIES = booleanPreferencesKey("hide_stories")
        val HIDE_PEOPLE_YOU_MAY_KNOW = booleanPreferencesKey("hide_people_you_may_know")
        val HIDE_GROUPS = booleanPreferencesKey("hide_groups")

        val isRevertDesktop = booleanPreferencesKey("is_revert_desktop")
    }

    val revertDesktop = context.dataStore.data.map { it[isRevertDesktop] == true }
    suspend fun setRevertDesktop(revertDesktop: Boolean) {
        context.dataStore.edit { it[isRevertDesktop] = revertDesktop }
    }

    val removeAds = context.dataStore.data.map { it[REMOVE_ADS] != false }
    suspend fun setRemoveAds(removeAds: Boolean) {
        context.dataStore.edit { it[REMOVE_ADS] = removeAds }
    }

    val enableDownloadContent = context.dataStore.data.map { it[ENABLE_DOWNLOAD_CONTENT] != false }
    suspend fun setEnableDownloadContent(enableDownloadContent: Boolean) {
        context.dataStore.edit { it[ENABLE_DOWNLOAD_CONTENT] = enableDownloadContent }
    }

    val desktopLayout = context.dataStore.data.map { it[DESKTOP_LAYOUT] == true }
    suspend fun setDesktopLayout(desktopLayout: Boolean) {
        context.dataStore.edit { it[DESKTOP_LAYOUT] = desktopLayout }
    }

    val immersiveMode = context.dataStore.data.map { it[IMMERSIVE_MODE] == true }
    suspend fun setImmersiveMode(immersiveMode: Boolean) {
        context.dataStore.edit { it[IMMERSIVE_MODE] = immersiveMode }
    }

    val stickyNavbar = context.dataStore.data.map { it[STICKY_NAVBAR] != false }
    suspend fun setStickyNavbar(stickyNavbar: Boolean) {
        context.dataStore.edit { it[STICKY_NAVBAR] = stickyNavbar }
    }

    val pinchToZoom = context.dataStore.data.map { it[PINCH_TO_ZOOM] == true }
    suspend fun setPinchToZoom(pinchToZoom: Boolean) {
        context.dataStore.edit { it[PINCH_TO_ZOOM] = pinchToZoom }
    }

    val amoledBlack = context.dataStore.data.map { it[AMOLED_BLACK] == true }
    suspend fun setAmoledBlack(amoledBlack: Boolean) {
        context.dataStore.edit { it[AMOLED_BLACK] = amoledBlack }
    }

    val hideSuggested = context.dataStore.data.map { it[HIDE_SUGGESTED] == true }
    suspend fun setHideSuggested(hideSuggestion: Boolean) {
        context.dataStore.edit { it[HIDE_SUGGESTED] = hideSuggestion }
    }

    val hideReels = context.dataStore.data.map { it[HIDE_REELS] == true }
    suspend fun setHideReels(hideReels: Boolean) {
        context.dataStore.edit { it[HIDE_REELS] = hideReels }
    }

    val hideStories = context.dataStore.data.map { it[HIDE_STORIES] == true }
    suspend fun setHideStories(hideStories: Boolean) {
        context.dataStore.edit { it[HIDE_STORIES] = hideStories }
    }

    val hidePeopleYouMayKnow = context.dataStore.data.map { it[HIDE_PEOPLE_YOU_MAY_KNOW] == true }
    suspend fun setHidePeopleYouMayKnow(hidePeopleYouMayKnow: Boolean) {
        context.dataStore.edit { it[HIDE_PEOPLE_YOU_MAY_KNOW] = hidePeopleYouMayKnow }
    }

    val hideGroups = context.dataStore.data.map { it[HIDE_GROUPS] == true }
    suspend fun setHideGroups(hideGroups: Boolean) {
        context.dataStore.edit { it[HIDE_GROUPS] = hideGroups }
    }
}