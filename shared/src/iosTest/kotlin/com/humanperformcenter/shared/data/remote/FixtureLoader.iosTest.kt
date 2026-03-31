package com.humanperformcenter.shared.data.remote

internal actual fun readCommonTestResource(path: String): String {
    error("iOS fixture loading is not configured for this test runtime. Missing: $path")
}
