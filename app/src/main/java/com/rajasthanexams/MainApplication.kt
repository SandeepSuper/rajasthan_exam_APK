package com.rajasthanexams

import android.app.Application
import com.rajasthanexams.data.remote.RetrofitClient

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        RetrofitClient.init(this)
    }

    companion object {
        lateinit var instance: MainApplication
            private set
    }
}
