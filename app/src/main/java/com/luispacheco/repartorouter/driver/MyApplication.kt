package com.luispacheco.repartorouter.driver

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.luispacheco.repartorouter.driver.data.local.TokenManager
import com.luispacheco.repartorouter.driver.data.remote.RetrofitClient
import com.luispacheco.repartorouter.driver.notifications.RepartoMessagingService

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val tokenManager = TokenManager(applicationContext)
        RetrofitClient.init(tokenManager)
        crearCanalNotificaciones()
    }

    private fun crearCanalNotificaciones() {
        val canal = NotificationChannel(
            RepartoMessagingService.CANAL_ID,
            "Notificaciones de rutas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos de nuevas rutas asignadas"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(canal)
    }
}