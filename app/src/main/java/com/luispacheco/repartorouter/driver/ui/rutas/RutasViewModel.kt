package com.luispacheco.repartorouter.driver.ui.rutas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luispacheco.repartorouter.driver.domain.repository.RutaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RutasViewModel(
    private val rutaRepository: RutaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RutasUiState>(RutasUiState.Cargando)
    val uiState: StateFlow<RutasUiState> = _uiState.asStateFlow()

    init {
        cargarRutas()
    }

    fun cargarRutas() {
        viewModelScope.launch {
            _uiState.value = RutasUiState.Cargando
            rutaRepository.obtenerRutas()
                .onSuccess { rutas ->
                    _uiState.value = RutasUiState.Exito(rutas)
                }
                .onFailure { error ->
                    _uiState.value = RutasUiState.Error(
                        error.message ?: "Error desconocido al cargar las rutas"
                    )
                }
        }
    }
}