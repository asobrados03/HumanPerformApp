package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.EstadisticasUsuario
import com.humanperformcenter.shared.domain.usecase.UserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EstadisticasUsuarioViewModel(
    private val userUseCase: UserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadisticasUsuario())
    val uiState: StateFlow<EstadisticasUsuario> = _uiState.asStateFlow()

    fun cargarEstadisticas(userId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val stats = userUseCase.getUserStats(userId)
                _uiState.value = EstadisticasUsuario(
                    entrenamientosMesPasado = stats.entrenamientosMesPasado,
                    entrenadorMasUsado = stats.entrenadorMasUsado,
                    isLoading = false,
                    error = null
                )
                println("Estadísticas cargadas: ${stats.entrenamientosMesPasado} entrenamientos, ${stats.entrenadorMasUsado}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }
}

