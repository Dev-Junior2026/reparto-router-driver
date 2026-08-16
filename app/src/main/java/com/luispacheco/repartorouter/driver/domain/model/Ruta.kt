package com.luispacheco.repartorouter.driver.domain.model

data class Ruta(
    val id: Long? = null,
    val nombre: String,
    val horaInicio: String,             // "HH:mm:ss"
    val horaFinEstimada: String? = null,
    val distanciaTotalKm: Double,
    val tiempoTotalEstimado: String? = null,  // "PT2H30M"
    val paradasOrdenadas: List<Parada> = emptyList()
)