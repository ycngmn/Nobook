package com.ycngmn.nobook.utils

import androidx.annotation.RawRes


/**
 * Concatenate enabled scripts from packaged `res/raw/` resources.
 *
 * Earlier versions fetched each script from `raw.githubusercontent.com/ycngmn/Nobook`
 * on every ViewModel init/refresh, falling back to packaged `res/raw/` on failure.
 * For a fork, the packaged scripts are source-of-truth, so the remote fetch adds
 * only risk (stale-vs-packaged divergence) + extra work at construction.
 */
data class Script(
    val isEnabled: Boolean,
    @param:RawRes val resourceId: Int,
)

fun fetchScripts(
    scripts: List<Script>,
    fallbackContent: (Int) -> String
): String {
    return buildString {
        scripts.filter { it.isEnabled }.forEach { script ->
            append(fallbackContent(script.resourceId))
        }
    }
}
