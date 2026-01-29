package com.ycngmn.nobook.utils

import java.nio.file.Paths

fun getAuthPath() =
    runCatching {
        Paths.get("src/test/resources/auth.json")
    }.getOrNull()
