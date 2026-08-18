package com.luispacheco.repartorouter.driver.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.luispacheco.repartorouter.driver.MainActivity
import com.luispacheco.repartorouter.driver.R
import com.luispacheco.repartorouter.driver.data.local.TokenManager
import com.luispacheco.repartorouter.driver.data.remote.RetrofitClient
import com.luispacheco.repartorouter.driver.data.remote.dto.TokenFcmRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RepartoMessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job)

    companion object {
        private const val TAG = "RepartoMessaging"
        const val CANAL_ID = "reparto_router_canal"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Token FCM nuevo generado: $token")

        scope.launch {
            val tokenManager = TokenManager(applicationContext)
            val jwt = tokenManager.obtenerToken()
            if (jwt != null) {
                enviarTokenAlBackend(token)
            } else {
                Log.d(TAG, "No hay sesión activa todavía, no se envía el token")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Mensaje recibido de: ${message.from}")

        val titulo = message.data["title"] ?: "Reparto Router"
        val cuerpo = message.data["body"] ?: "Tienes una actualización de ruta"
        val rutaId = message.data["rutaId"]

        mostrarNotificacion(titulo, cuerpo, rutaId)
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String, rutaId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            rutaId?.let { putExtra("rutaId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(this, CANAL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(System.currentTimeMillis().toInt(), notificacion)
    }

    private suspend fun enviarTokenAlBackend(token: String) {
        try {
            val response = RetrofitClient.rutaApiService.actualizarTokenFcm(TokenFcmRequest(token))
            if (response.isSuccessful) {
                Log.d(TAG, "Token FCM enviado y guardado correctamente")
            } else {
                Log.e(TAG, "Error del servidor al guardar token: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error de red al enviar token FCM", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}