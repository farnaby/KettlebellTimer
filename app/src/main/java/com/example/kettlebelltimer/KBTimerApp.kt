package com.example.kettlebelltimer

import android.app.Application
import android.content.Context

class KBTimerApp : Application() {
    // Companion object to hold the singleton instance
    companion object {
        @Volatile
        private var INSTANCE: AudioManager? = null

        fun getAudioManager(context: Context): AudioManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioManager(context.applicationContext).also { INSTANCE = it }
            }
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize AudioManager when the application starts
        // This ensures it's ready when needed and uses the application context.
        getAudioManager(this)
    }
}