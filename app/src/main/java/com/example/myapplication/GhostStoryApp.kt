package com.example.myapplication

import android.app.Application
import org.osmdroid.config.Configuration
import java.io.File

class GhostStoryApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize osmdroid configuration
        val config = Configuration.getInstance()
        config.userAgentValue = packageName

        // Set tile cache to app's cache directory (no permissions needed)
        val tileCacheDir = File(cacheDir, "osmdroid")
        if (!tileCacheDir.exists()) {
            tileCacheDir.mkdirs()
        }
        config.osmdroidTileCache = tileCacheDir
        config.osmdroidBasePath = tileCacheDir
    }
}
