package com.luispacheco.repartorouter.driver.data.remote

import com.luispacheco.repartorouter.driver.domain.model.Parada
import com.luispacheco.repartorouter.driver.domain.model.Ruta
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface RutaApiService {

    @GET("api/rutas/mis-rutas")
    suspend fun listarRutas(): Response<List<Ruta>>

    @GET("api/rutas/{id}")
    suspend fun obtenerRuta(@Path("id") id: Long): Response<Ruta>

    @GET("api/rutas/{rutaId}/paradas")
    suspend fun listarParadas(@Path("rutaId") rutaId: Long): Response<List<Parada>>
}