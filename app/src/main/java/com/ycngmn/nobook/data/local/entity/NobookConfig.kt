package com.ycngmn.nobook.data.local.entity

data class NobookConfig(
    val removeAds: Boolean,
    val enableDownloadContent: Boolean,
    val enableCopyToClipboard: Boolean,
    val desktopLayout: Boolean,
    val immersiveMode: Boolean,
    val stickyNavbar: Boolean,
    val pinchToZoom: Boolean,
    val amoledBlack: Boolean,
    val hideSuggested: Boolean,
    val hideReels: Boolean,
    val hideStories: Boolean,
    val hidePeopleYouMayKnow: Boolean,
    val hideGroups: Boolean
)