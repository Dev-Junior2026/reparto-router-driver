package com.luispacheco.repartorouter.driver.ui.rutas

import com.luispacheco.repartorouter.driver.domain.model.Ruta

sealed interface RutasUiState {
    data object Cargando : RutasUiState
    data class Exito(val rutas: List<Ruta>) : RutasUiState
    data class Error(val mensaje: String) : RutasUiState
}