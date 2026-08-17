package com.luispacheco.repartorouter.driver

import android.app.Application
import com.luispacheco.repartorouter.driver.data.local.TokenManager
import com.luispacheco.repartorouter.driver.data.remote.RetrofitClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val tokenManager = TokenManager(applicationContext)
        RetrofitClient.init(tokenManager)
    }
}