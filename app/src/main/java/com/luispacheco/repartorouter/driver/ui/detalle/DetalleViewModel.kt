package com.luispacheco.repartorouter.driver.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luispacheco.repartorouter.driver.domain.model.EstadoParada
import com.luispacheco.repartorouter.driver.domain.repository.RutaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleViewModel(
    private val rutaRepository: RutaRepository,
    private val rutaId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleUiState>(DetalleUiState.Cargando)
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        cargarRuta()
    }

    fun cargarRuta() {
        viewModelScope.launch {
            _uiState.value = DetalleUiState.Cargando
            rutaRepository.obtenerRutaPorId(rutaId)
                .onSuccess { ruta ->
                    _uiState.value = DetalleUiState.Exito(ruta)
                }
                .onFailure { error ->
                    _uiState.value = DetalleUiState.Error(
                        error.message ?: "Error al cargar la ruta"
                    )
                }
        }
    }

    /**
     * Cambia el estado de una parada (PENDIENTE / ENTREGADO / RECHAZADO). Actualiza
     * la UI al instante (optimistic update) y revierte al estado anterior real
     * si la llamada al servidor falla.
     */
    fun cambiarEstadoParada(paradaId: Long, nuevoEstado: EstadoParada) {
        val estadoActual = _uiState.value
        if (estadoActual !is DetalleUiState.Exito) return

        val ruta = estadoActual.ruta
        val estadoAnterior = ruta.paradasOrdenadas.firstOrNull { it.id == paradaId }?.estado
            ?: EstadoParada.PENDIENTE

        val paradasActualizadas = ruta.paradasOrdenadas.map { parada ->
            if (parada.id == paradaId) parada.copy(estado = nuevoEstado) else parada
        }

        // Aplica el cambio de inmediato en la UI
        _uiState.value = DetalleUiState.Exito(ruta.copy(paradasOrdenadas = paradasActualizadas))

        viewModelScope.launch {
            rutaRepository.actualizarEstadoParada(rutaId, paradaId, nuevoEstado)
                .onFailure {
                    // Revertimos al estado anterior real si el servidor no confirma el cambio
                    val paradasRevertidas = ruta.paradasOrdenadas.map { parada ->
                        if (parada.id == paradaId) parada.copy(estado = estadoAnterior) else parada
                    }
                    _uiState.value = DetalleUiState.Exito(ruta.copy(paradasOrdenadas = paradasRevertidas))
                }
        }
    }
}