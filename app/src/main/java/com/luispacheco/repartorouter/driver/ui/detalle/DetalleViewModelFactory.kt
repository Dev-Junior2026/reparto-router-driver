package com.luispacheco.repartorouter.driver.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.luispacheco.repartorouter.driver.domain.repository.RutaRepository

class DetalleViewModelFactory(
    private val rutaRepository: RutaRepository,
    private val rutaId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetalleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetalleViewModel(rutaRepository, rutaId) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}