package com.luispacheco.repartorouter.driver.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
     * Marca/desmarca una parada como completada. Actualiza la UI al instante
     * (optimistic update) y revierte el cambio si la llamada al servidor falla.
     */
    fun toggleParadaCompletada(paradaId: Long, nuevoEstado: Boolean) {
        val estadoActual = _uiState.value
        if (estadoActual !is DetalleUiState.Exito) return

        val ruta = estadoActual.ruta
        val paradasActualizadas = ruta.paradasOrdenadas.map { parada ->
            if (parada.id == paradaId) parada.copy(completada = nuevoEstado) else parada
        }

        // Aplica el cambio de inmediato en la UI
        _uiState.value = DetalleUiState.Exito(ruta.copy(paradasOrdenadas = paradasActualizadas))

        viewModelScope.launch {
            rutaRepository.actualizarEstadoParada(rutaId, paradaId, nuevoEstado)
                .onFailure {
                    // Revertimos si el servidor no confirma el cambio
                    val paradasRevertidas = ruta.paradasOrdenadas.map { parada ->
                        if (parada.id == paradaId) parada.copy(completada = !nuevoEstado) else parada
                    }
                    _uiState.value = DetalleUiState.Exito(ruta.copy(paradasOrdenadas = paradasRevertidas))
                }
        }
    }
}