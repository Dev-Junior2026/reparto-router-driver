package com.luispacheco.repartorouter.driver.domain.model

data class Parada(
    val id: Long? = null,
    val numero: Int,
    val nombre: String,
    val calle: String,
    val codigoPostal: String,
    val poblacion: String,
    val latitud: Double,
    val longitud: Double,
    val horaApertura: String,      // "HH:mm:ss"
    val horaCierre: String,        // "HH:mm:ss"
    val tiempoDescargaMin: Int,
    val esAlmacen: Boolean,
    val horaLlegadaEstimada: String? = null
) {
    val direccion: String
        get() = "$calle $codigoPostal $poblacion"
}

