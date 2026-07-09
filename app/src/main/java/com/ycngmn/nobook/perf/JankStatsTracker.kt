package com.ycngmn.nobook.perf

import android.app.Activity
import android.util.Log
import androidx.metrics.performance.JankStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object JankStatsTracker {
    private const val TAG = "NobookJankStats"
    private val tsFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun attach(activity: Activity): () -> Unit {
        JankStats.createAndTrack(activity.window) { frameState ->
            if (!frameState.isJank) return@createAndTrack
            val ts = tsFormat.format(Date())
            val durationMs = frameState.frameDurationUiNanos / 1_000_000.0
            Log.i(TAG, "$ts,jank,${"%.2f".format(durationMs)},${frameState.frameStartNanos}")
        }
        return {}
    }
}
