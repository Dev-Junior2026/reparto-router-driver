package com.luispacheco.repartorouter.driver.domain.repository

import com.luispacheco.repartorouter.driver.domain.model.Parada
import com.luispacheco.repartorouter.driver.domain.model.Ruta

interface RutaRepository {
    suspend fun obtenerRutas(): Result<List<Ruta>>
    suspend fun obtenerRutaPorId(id: Long): Result<Ruta>
    suspend fun actualizarEstadoParada(rutaId: Long, paradaId: Long, completada: Boolean): Result<Parada>
}