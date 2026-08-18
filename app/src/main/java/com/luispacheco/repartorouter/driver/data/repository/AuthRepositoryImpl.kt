package com.luispacheco.repartorouter.driver.data.repository

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.luispacheco.repartorouter.driver.data.local.TokenManager
import com.luispacheco.repartorouter.driver.data.remote.AuthApiService
import com.luispacheco.repartorouter.driver.data.remote.RutaApiService
import com.luispacheco.repartorouter.driver.data.remote.dto.LoginRequest
import com.luispacheco.repartorouter.driver.data.remote.dto.TokenFcmRequest
import com.luispacheco.repartorouter.driver.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val rutaApiService: RutaApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    tokenManager.guardarSesion(body.token, body.choferId, body.nombre)
                    enviarTokenFcmActual()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                Result.failure(Exception("Email o contraseña incorrectos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun enviarTokenFcmActual() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            rutaApiService.actualizarTokenFcm(TokenFcmRequest(token))
        } catch (e: Exception) {
            // No bloqueamos el login si esto falla; solo lo registramos.
            Log.e("AuthRepositoryImpl", "No se pudo enviar el token FCM tras login", e)
        }
    }
}