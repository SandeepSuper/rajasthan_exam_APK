package com.rajasthanexams

import android.app.Application
import com.rajasthanexams.data.remote.NotificationWebSocketClient
import com.rajasthanexams.data.remote.RetrofitClient

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        RetrofitClient.init(this)
        // Connect WebSocket at app start so notifications are received instantly
        // from any screen, not just when Notifications screen is opened.
        NotificationWebSocketClient.connect()
    }

    companion object {
        lateinit var instance: MainApplication
            private set
    }
}
