package com.rajasthanexams

import android.app.Application
import com.rajasthanexams.data.remote.RetrofitClient

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
    }
}
