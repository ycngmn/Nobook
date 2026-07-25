package com.ycngmn.nobook.utils

import androidx.annotation.RawRes

data class Script(
    val isEnabled: Boolean,
    @param:RawRes val resourceId: Int,
)

fun loadBundledScripts(
    scripts: List<Script>,
    contentProvider: (Int) -> String,
): String {
    return buildString {
        scripts
            .asSequence()
            .filter { it.isEnabled }
            .forEach { script ->
                append(contentProvider(script.resourceId))
                append('\n')
            }
    }
}