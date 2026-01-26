package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.data.model.user.UserStatistics
import com.humanperformcenter.shared.domain.usecase.UserUseCase
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserStatsViewModel(
    private val userUseCase: UserUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserStatistics())
    @NativeCoroutinesState
    val uiState: StateFlow<UserStatistics> = _uiState.asStateFlow()

    fun loadStatistics(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = userUseCase.getUserStats(userId)

            result.onSuccess { stats ->
                _uiState.value = _uiState.value.copy(
                    entrenamientosMesPasado = stats.entrenamientosMesPasado,
                    entrenadorMasUsado = stats.entrenadorMasUsado,
                    reservasPendientes = stats.reservasPendientes,
                    isLoading = false,
                    error = null
                )
                println("Estadísticas cargadas con éxito")
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Error al cargar estadísticas"
                )
            }
        }
    }
}