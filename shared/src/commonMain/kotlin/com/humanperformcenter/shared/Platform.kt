package com.humanperformcenter.shared

class PlatformBridge {
    fun platformName(): String = platform()
}

expect fun platform(): String