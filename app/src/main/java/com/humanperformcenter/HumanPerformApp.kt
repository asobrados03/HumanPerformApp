package com.humanperformcenter

import android.app.Application

class HumanPerformApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }
}