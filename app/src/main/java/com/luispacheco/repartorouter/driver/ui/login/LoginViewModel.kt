package com.luispacheco.repartorouter.driver.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luispacheco.repartorouter.driver.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(nuevoEmail: String) {
        _uiState.update { it.copy(email = nuevoEmail, error = null) }
    }

    fun onPasswordChange(nuevaPassword: String) {
        _uiState.update { it.copy(password = nuevaPassword, error = null) }
    }

    fun login() {
        val estado = _uiState.value

        if (estado.email.isBlank() || estado.password.isBlank()) {
            _uiState.update { it.copy(error = "Rellena email y contraseña") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }

            val resultado = authRepository.login(estado.email, estado.password)

            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(cargando = false, loginExitoso = true) }
                },
                onFailure = { excepcion ->
                    _uiState.update {
                        it.copy(cargando = false, error = excepcion.message ?: "Error desconocido")
                    }
                }
            )
        }
    }
}