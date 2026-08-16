package com.luispacheco.repartorouter.driver.data.repository

import com.luispacheco.repartorouter.driver.data.remote.RutaApiService
import com.luispacheco.repartorouter.driver.domain.model.Ruta
import com.luispacheco.repartorouter.driver.domain.repository.RutaRepository

class RutaRepositoryImpl(
    private val apiService: RutaApiService
) : RutaRepository {

    override suspend fun obtenerRutas(): Result<List<Ruta>> {
        return try {
            val response = apiService.listarRutas()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerRutaPorId(id: Long): Result<Ruta> {
        return try {
            val response = apiService.obtenerRuta(id)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Ruta no encontrada"))
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}