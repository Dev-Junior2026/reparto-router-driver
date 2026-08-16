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
}