package com.muchbeer.eventscanner

import android.app.Application
import logcat.AndroidLogcatLogger
import logcat.LogPriority

class ScanApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = LogPriority.VERBOSE)
    }
}