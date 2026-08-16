package com.luispacheco.repartorouter.driver.ui.detalle

import com.luispacheco.repartorouter.driver.domain.model.Ruta

sealed interface DetalleUiState {
    data object Cargando : DetalleUiState
    data class Exito(val ruta: Ruta) : DetalleUiState
    data class Error(val mensaje: String) : DetalleUiState
}