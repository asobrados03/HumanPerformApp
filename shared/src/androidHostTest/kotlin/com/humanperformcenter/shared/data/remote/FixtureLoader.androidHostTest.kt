package com.humanperformcenter.shared.data.remote

import java.io.File

internal actual fun readCommonTestResource(path: String): String {
    val normalized = path.removePrefix("/")
    val classLoader = Thread.currentThread().contextClassLoader
    val stream = classLoader?.getResourceAsStream(normalized)
        ?: object {}.javaClass.classLoader?.getResourceAsStream(normalized)

    if (stream != null) {
        return stream.bufferedReader().use { it.readText() }
    }

    val fromProject = File("shared/src/commonTest/resources/$normalized")
    check(fromProject.exists()) { "Fixture not found: $path" }
    return fromProject.readText()
}
