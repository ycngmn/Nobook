package com.ycngmn.nobook.utils

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext

const val PIXEL_6_WIDTH = 394
const val PIXEL_6_HEIGHT = 875

const val PIXEL_6_UA =
    "Mozilla/5.0 (Linux; Android 13; Pixel 6) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Mobile Safari/537.36"

fun Browser.nbTestContext(): BrowserContext =
    this.newContext(
        Browser.NewContextOptions()
            .setStorageStatePath(
                getAuthPath() ?: throw Exception("Auth file not found")
            )
            .setViewportSize(PIXEL_6_WIDTH, PIXEL_6_HEIGHT)
            .setUserAgent(PIXEL_6_UA)
            .setDeviceScaleFactor(3.0)
            .setIsMobile(true)
            .setHasTouch(true)
    )