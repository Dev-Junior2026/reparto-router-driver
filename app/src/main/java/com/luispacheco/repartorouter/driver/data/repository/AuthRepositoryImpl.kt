package com.luispacheco.repartorouter.driver.data.repository

import com.luispacheco.repartorouter.driver.data.local.TokenManager
import com.luispacheco.repartorouter.driver.data.remote.AuthApiService
import com.luispacheco.repartorouter.driver.data.remote.dto.LoginRequest
import com.luispacheco.repartorouter.driver.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    tokenManager.guardarSesion(body.token, body.choferId, body.nombre)
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
}