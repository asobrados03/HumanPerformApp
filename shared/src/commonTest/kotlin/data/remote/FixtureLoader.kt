package com.humanperformcenter.shared.data.remote

internal expect fun readCommonTestResource(path: String): String

internal fun fixtureJson(domain: String, fileName: String): String =
    readCommonTestResource("fixtures/$domain/$fileName")
