package com.ycngmn.nobook.startup

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.startup.Initializer
import com.ycngmn.nobook.data.local.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Pre-warms the Chromium WebView engine during app startup so the first
 * [com.ycngmn.nobook.ui.screens.NobookWebView] creation by MainActivity is faster.
 *
 * Opt-in via the user "Fast cold-launch" toggle (default on). When disabled the
 * initializer exits immediately; no WebView is constructed.
 *
 * androidx.startup [Initializer.create] runs on a background thread but WebView
 * construction must happen on the UI thread, so we post the warmup to the main
 * looper and return immediately — the background initialization completes
 * before MainActivity reaches setContent.
 *
 * We do not call [WebView.loadUrl] here: pre-warming the engine's native libs,
 * factory provider, and [CookieManager] singletons is enough to drop the
 * "gray-screen at cold launch" first-frame time. Cookies are shared process-wide
 * via [CookieManager.getInstance], so the visible WebView created later inherits
 * any session accepted here.
 */
class WebViewInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val toggle = runBlocking { SettingsDataStore(context).fastColdLaunch.first() }
        if (toggle != true) return

        Handler(Looper.getMainLooper()).post {
            try {
                val webView = WebView(context.applicationContext)
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true
                // Intentionally: no loadUrl. Don't destroy — let GC clean up.
            } catch (e: Throwable) {
                // Swallow — pre-warm is best-effort; never crash app launch.
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
