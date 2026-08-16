package com.luispacheco.repartorouter.driver.ui.rutas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.luispacheco.repartorouter.driver.domain.repository.RutaRepository

class RutasViewModelFactory(
    private val rutaRepository: RutaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RutasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RutasViewModel(rutaRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}